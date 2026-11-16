package com.fypgrading.adminservice.grading;

import com.fypgrading.adminservice.common.security.CurrentUser;
import com.fypgrading.adminservice.common.web.ApiException;
import com.fypgrading.adminservice.domain.Assessment;
import com.fypgrading.adminservice.domain.Grade;
import com.fypgrading.adminservice.domain.GradeRepository;
import com.fypgrading.adminservice.domain.TeamAssessmentRepository;
import com.fypgrading.adminservice.domain.TeamRepository;
import com.fypgrading.adminservice.domain.TeamReviewer;
import com.fypgrading.adminservice.domain.TeamReviewerRepository;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Receives a submitted evaluation's score from evaluation-service and aggregates it in the same transaction, under
 * the team lock: a grade can't be stored without its team grade being recomputed (review M1), two hand-offs for one
 * team can't race (M2), and a reviewer-set change can't interleave (H1). The announcements (notifications, the
 * activity feed) still go out over RabbitMQ after the commit.
 */
@Service
public class GradeIntake {

    private final TeamRepository teams;
    private final TeamReviewerRepository teamReviewers;
    private final GradeRepository grades;
    private final TeamAssessmentRepository teamAssessments;
    private final GradeAggregator aggregator;

    public GradeIntake(
            TeamRepository teams,
            TeamReviewerRepository teamReviewers,
            GradeRepository grades,
            TeamAssessmentRepository teamAssessments,
            GradeAggregator aggregator) {
        this.teams = teams;
        this.teamReviewers = teamReviewers;
        this.grades = grades;
        this.teamAssessments = teamAssessments;
        this.aggregator = aggregator;
    }

    /**
     * {@code submittedLate}: the evaluation was submitted after the due date in force at that moment. It's stored,
     * so moving the due date later never clears it (review M12).
     */
    public record GradeSubmission(
            @NotNull UUID evaluationId,
            @NotNull UUID teamId,
            @NotNull Assessment assessment,
            @NotNull UUID reviewerId,
            @DecimalMin("0") @DecimalMax("100") double score,
            @NotNull Instant submittedAt,
            boolean submittedLate) {}

    public record Receipt(int submittedCount, int expectedCount) {}

    @Transactional
    public Receipt submit(GradeSubmission s) {
        // Review M8: the caller forwards the reviewer's own token; nobody hands in someone else's grade.
        if (!CurrentUser.require().id().equals(s.reviewerId())) {
            throw ApiException.forbidden("not-your-grade", "A reviewer can only hand in their own grade.");
        }
        teams.lockById(s.teamId()).orElseThrow(() -> ApiException.notFound("The team"));
        TeamReviewer assignment = teamReviewers
                .findByTeamIdAndReviewerId(s.teamId(), s.reviewerId())
                .filter(r -> r.getRole() == s.assessment().role())
                .orElseThrow(() -> ApiException.forbidden(
                        "not-assigned",
                        "This reviewer doesn't grade the " + s.assessment().displayName() + " for this team."));

        Optional<Grade> same = grades.findByEvaluationId(s.evaluationId());
        if (same.isPresent()) {
            // A retried hand-off: fine if it's the same grade, refused if the score changed in between (review H2).
            if (Math.abs(same.get().getScore() - s.score()) > 1e-9) {
                throw ApiException.conflict(
                        "grade-mismatch", "This evaluation was already handed in with a different score.");
            }
            return receipt(s);
        }
        if (teamAssessments
                .findByTeamIdAndAssessment(s.teamId(), s.assessment())
                .isPresent()) {
            throw ApiException.conflict(
                    "assessment-final",
                    "The " + s.assessment().displayName() + " grade for this team is already final.");
        }
        if (grades.findByTeamIdAndAssessmentAndReviewerId(s.teamId(), s.assessment(), assignment.getReviewerId())
                .isPresent()) {
            throw ApiException.conflict(
                    "grade-exists",
                    "This reviewer already submitted the " + s.assessment().displayName() + ".");
        }
        grades.save(new Grade(
                UUID.randomUUID(),
                s.evaluationId(),
                s.teamId(),
                s.assessment(),
                s.reviewerId(),
                s.score(),
                s.submittedAt(),
                s.submittedLate()));
        grades.flush();
        aggregator.aggregate(s.teamId(), s.assessment(), true);
        return receipt(s);
    }

    private Receipt receipt(GradeSubmission s) {
        long expected = teamReviewers.findByTeamId(s.teamId()).stream()
                .filter(r -> r.getRole() == s.assessment().role())
                .count();
        int submitted =
                grades.findByTeamIdAndAssessment(s.teamId(), s.assessment()).size();
        return new Receipt(submitted, (int) expected);
    }
}
