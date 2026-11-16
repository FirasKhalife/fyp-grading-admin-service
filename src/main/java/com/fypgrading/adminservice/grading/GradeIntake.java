package com.fypgrading.adminservice.grading;

import com.fypgrading.adminservice.common.demo.DemoSession;
import com.fypgrading.adminservice.common.web.ApiException;
import com.fypgrading.adminservice.domain.Assessment;
import com.fypgrading.adminservice.domain.Grade;
import com.fypgrading.adminservice.domain.GradeRepository;
import com.fypgrading.adminservice.domain.TeamRepository;
import com.fypgrading.adminservice.domain.TeamReviewer;
import com.fypgrading.adminservice.domain.TeamReviewerRepository;
import com.fypgrading.adminservice.events.Events;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Consumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Receives a submitted evaluation's score from evaluation-service. The reviewer must hold the assessment's role
 * on the team. Idempotent on the evaluation id. Aggregation happens asynchronously over {@code assay.grades}.
 */
@Service
public class GradeIntake {

    private final TeamRepository teams;
    private final TeamReviewerRepository teamReviewers;
    private final GradeRepository grades;
    private final Events events;

    public GradeIntake(
            TeamRepository teams, TeamReviewerRepository teamReviewers, GradeRepository grades, Events events) {
        this.teams = teams;
        this.teamReviewers = teamReviewers;
        this.grades = grades;
        this.events = events;
    }

    public record GradeSubmission(
            @NotNull UUID evaluationId,
            @NotNull UUID teamId,
            @NotNull Assessment assessment,
            @NotNull UUID reviewerId,
            @DecimalMin("0") @DecimalMax("100") double score,
            @NotNull Instant submittedAt) {}

    public record Receipt(int submittedCount, int expectedCount) {}

    @Transactional
    public Receipt submit(GradeSubmission s) {
        teams.findById(s.teamId()).orElseThrow(() -> ApiException.notFound("The team"));
        TeamReviewer assignment = teamReviewers
                .findByTeamIdAndReviewerId(s.teamId(), s.reviewerId())
                .filter(r -> r.getRole() == s.assessment().role())
                .orElseThrow(() -> ApiException.forbidden(
                        "not-assigned",
                        "This reviewer doesn't grade the " + s.assessment().displayName() + " for this team."));
        boolean alreadyStored = grades.findByEvaluationId(s.evaluationId()).isPresent();
        if (!alreadyStored) {
            grades.findByTeamIdAndAssessmentAndReviewerId(s.teamId(), s.assessment(), assignment.getReviewerId())
                    .ifPresent(g -> {
                        throw ApiException.conflict(
                                "grade-exists",
                                "This reviewer already submitted the "
                                        + s.assessment().displayName() + ".");
                    });
            grades.save(new Grade(
                    UUID.randomUUID(),
                    s.evaluationId(),
                    s.teamId(),
                    s.assessment(),
                    s.reviewerId(),
                    s.score(),
                    s.submittedAt()));
        }
        // Published even on a retry: aggregation is idempotent and this heals a lost message.
        events.gradeSubmitted(s.teamId(), s.assessment());
        long expected = teamReviewers.findByTeamId(s.teamId()).stream()
                .filter(r -> r.getRole() == s.assessment().role())
                .count();
        int submitted =
                grades.findByTeamIdAndAssessment(s.teamId(), s.assessment()).size();
        return new Receipt(submitted, (int) expected);
    }

    /** The {@code assay.grades} consumer. */
    @Configuration
    static class Consumers {

        @Bean
        Consumer<Events.GradeSubmitted> aggregate(GradeAggregator aggregator) {
            return event -> DemoSession.runAs(
                    UUID.fromString(event.demoSession()),
                    () -> aggregator.aggregate(UUID.fromString(event.teamId()), event.assessment(), true));
        }
    }
}
