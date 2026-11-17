package com.fypgrading.adminservice.grading;

import com.fypgrading.adminservice.common.demo.DemoSession;
import com.fypgrading.adminservice.common.demo.SeedIds;
import com.fypgrading.adminservice.domain.Assessment;
import com.fypgrading.adminservice.domain.Grade;
import com.fypgrading.adminservice.domain.GradeRepository;
import com.fypgrading.adminservice.domain.Reviewer;
import com.fypgrading.adminservice.domain.ReviewerRepository;
import com.fypgrading.adminservice.domain.Team;
import com.fypgrading.adminservice.domain.TeamAssessment;
import com.fypgrading.adminservice.domain.TeamAssessmentRepository;
import com.fypgrading.adminservice.domain.TeamRepository;
import com.fypgrading.adminservice.domain.TeamReviewer;
import com.fypgrading.adminservice.domain.TeamReviewerRepository;
import com.fypgrading.adminservice.events.Events;
import java.time.Clock;
import java.time.Instant;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Turns reviewers' grades into team grades. Runs inside the grade hand-off's transaction (and when a demo copy
 * is seeded). Idempotent by construction: it recomputes from the stored grades and announces only state changes,
 * so a retried hand-off can't double-count or notify twice. The team row is locked, so two last
 * grades arriving together can't both finalise.
 */
@Service
public class GradeAggregator {

    private final TeamRepository teams;
    private final TeamReviewerRepository teamReviewers;
    private final GradeRepository grades;
    private final TeamAssessmentRepository teamAssessments;
    private final ReviewerRepository reviewers;
    private final Events events;
    private final Clock clock;
    private final double disagreementThreshold;

    public GradeAggregator(
            TeamRepository teams,
            TeamReviewerRepository teamReviewers,
            GradeRepository grades,
            TeamAssessmentRepository teamAssessments,
            ReviewerRepository reviewers,
            Events events,
            Clock clock,
            @Value("${assay.insights.disagreement-threshold:15}") double disagreementThreshold) {
        this.teams = teams;
        this.teamReviewers = teamReviewers;
        this.grades = grades;
        this.teamAssessments = teamAssessments;
        this.reviewers = reviewers;
        this.events = events;
        this.clock = clock;
        this.disagreementThreshold = disagreementThreshold;
    }

    public record Outcome(boolean assessmentBecameFinal, boolean finalGradeComputed) {}

    @Transactional
    public Outcome aggregate(UUID teamId, Assessment assessment, boolean announce) {
        Optional<Team> locked = teams.lockById(teamId);
        if (locked.isEmpty()) {
            return new Outcome(false, false);
        }
        Team team = locked.get();
        List<TeamReviewer> onTeam = teamReviewers.findByTeamId(teamId);
        Set<UUID> expected = new LinkedHashSet<>();
        onTeam.stream().filter(r -> r.getRole() == assessment.role()).forEach(r -> expected.add(r.getReviewerId()));
        List<Grade> submitted = grades.findByTeamIdAndAssessment(teamId, assessment).stream()
                .filter(g -> expected.contains(g.getReviewerId()))
                .toList();

        boolean becameFinal = false;
        Optional<TeamAssessment> existing = teamAssessments.findByTeamIdAndAssessment(teamId, assessment);
        if (!expected.isEmpty() && submitted.size() >= expected.size()) {
            double grade =
                    GradeMath.average(submitted.stream().map(Grade::getScore).toList());
            if (existing.isPresent()) {
                existing.get().regrade(grade);
            } else {
                Instant finalizedAt = submitted.stream()
                        .map(Grade::getSubmittedAt)
                        .max(Instant::compareTo)
                        .orElseThrow();
                teamAssessments.save(new TeamAssessment(
                        SeedIds.of(DemoSession.require(), "team-assessment:" + teamId + ":" + assessment),
                        teamId,
                        assessment,
                        grade,
                        finalizedAt));
                becameFinal = true;
                if (announce) {
                    announceAssessment(
                            team,
                            assessment,
                            grade,
                            submitted.stream().map(Grade::getScore).toList(),
                            onTeam);
                }
            }
        }

        boolean computed = false;
        Map<Assessment, Double> all = new EnumMap<>(Assessment.class);
        teamAssessments.findByTeamId(teamId).forEach(ta -> all.put(ta.getAssessment(), ta.getGrade()));
        Optional<Double> finalGrade = GradeMath.finalGrade(all);
        if (finalGrade.isPresent()) {
            boolean wasComputed = team.getFinalGrade() != null;
            Instant at = wasComputed
                    ? team.getFinalComputedAt()
                    : submitted.stream()
                            .map(Grade::getSubmittedAt)
                            .max(Instant::compareTo)
                            .orElse(clock.instant());
            team.setFinalGrade(finalGrade.get(), at);
            if (!wasComputed) {
                computed = true;
                if (announce) {
                    announceFinal(team, finalGrade.get(), onTeam);
                }
            }
        }
        return new Outcome(becameFinal, computed);
    }

    private void announceAssessment(
            Team team, Assessment assessment, double grade, List<Double> scores, List<TeamReviewer> onTeam) {
        String shown = String.format(java.util.Locale.ROOT, "%.1f", GradeMath.display(grade));
        // Plan 3.5: flag a large disagreement. The grade is still the plain average.
        String disagreement = GradeMath.disagree(scores, disagreementThreshold)
                ? " Reviewers disagree by "
                        + String.format(
                                "%.1f",
                                GradeMath.display(GradeMath.spread(scores).orElseThrow()))
                        + " points."
                : "";
        Instant now = clock.instant();
        events.notify(
                "grade-final:" + team.getId() + ":" + assessment,
                "GRADE_FINAL",
                assessment.displayName() + " for Team " + team.getName() + " is final: " + shown + "." + disagreement,
                now,
                Events.Link.team(team.getId(), assessment),
                staffOf(onTeam));
        events.activity(
                "GRADE_FINAL",
                null,
                "Team " + team.getName(),
                assessment.displayName() + " grade is final: " + shown + "." + disagreement,
                now,
                Events.Link.team(team.getId(), assessment));
    }

    private void announceFinal(Team team, double grade, List<TeamReviewer> onTeam) {
        String shown = String.format(java.util.Locale.ROOT, "%.1f", GradeMath.display(grade));
        Instant now = clock.instant();
        events.notify(
                "final-grade:" + team.getId(),
                "FINAL_GRADE_COMPUTED",
                "Team " + team.getName() + "'s final grade is computed: " + shown + ".",
                now,
                Events.Link.team(team.getId(), null),
                staffOf(onTeam));
        events.activity(
                "FINAL_GRADE_COMPUTED",
                null,
                "Team " + team.getName(),
                "final grade computed: " + shown + ".",
                now,
                Events.Link.team(team.getId(), null));
    }

    /** Coordinators and the team's reviewers (students wait for the release). */
    private Set<UUID> staffOf(List<TeamReviewer> onTeam) {
        Set<UUID> recipients = new LinkedHashSet<>();
        reviewers.findByCoordinatorTrue().stream().map(Reviewer::getId).forEach(recipients::add);
        onTeam.forEach(r -> recipients.add(r.getReviewerId()));
        return recipients;
    }
}
