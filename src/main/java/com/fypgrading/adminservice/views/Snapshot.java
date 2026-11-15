package com.fypgrading.adminservice.views;

import com.fypgrading.adminservice.clients.EvaluationClient.Summary;
import com.fypgrading.adminservice.domain.Assessment;
import com.fypgrading.adminservice.domain.DefenseSlot;
import com.fypgrading.adminservice.domain.Grade;
import com.fypgrading.adminservice.domain.Reviewer;
import com.fypgrading.adminservice.domain.ReviewerRole;
import com.fypgrading.adminservice.domain.Student;
import com.fypgrading.adminservice.domain.Team;
import com.fypgrading.adminservice.domain.TeamAssessment;
import com.fypgrading.adminservice.domain.TeamReviewer;
import com.fypgrading.adminservice.grading.GradeMath;
import com.fypgrading.adminservice.teams.Composition;
import com.fypgrading.adminservice.views.Views.ConflictView;
import com.fypgrading.adminservice.views.Views.DefenseSlotView;
import com.fypgrading.adminservice.views.Views.EvaluationStatus;
import com.fypgrading.adminservice.views.Views.FinalState;
import com.fypgrading.adminservice.views.Views.PersonView;
import com.fypgrading.adminservice.views.Views.ReviewerProgress;
import com.fypgrading.adminservice.views.Views.TeamAssessmentState;
import com.fypgrading.adminservice.views.Views.TeamAssessmentView;
import com.fypgrading.adminservice.views.Views.TeamRef;
import com.fypgrading.adminservice.views.Views.TeamReviewerView;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Everything in one demo copy, loaded once per request. A copy is small (six teams), so the screens are
 * derived in memory from one consistent read instead of many queries.
 */
public record Snapshot(Instant now, LocalDate anchor, List<Team> teams, List<Student> students,
                       List<TeamReviewer> assignments, List<Grade> grades, List<TeamAssessment> teamAssessments,
                       List<DefenseSlot> slots, Map<Assessment, Instant> dueDates, Map<UUID, Reviewer> reviewers,
                       List<Summary> evaluations) {

    public Optional<Team> team(UUID id) {
        return teams.stream().filter(t -> t.getId().equals(id)).findFirst();
    }

    public List<TeamReviewer> onTeam(UUID teamId) {
        return assignments.stream().filter(a -> a.getTeamId().equals(teamId))
                .sorted(Comparator.comparing((TeamReviewer a) -> a.getRole() == ReviewerRole.JURY)
                        .thenComparing(a -> reviewers.get(a.getReviewerId()).getLastName()))
                .toList();
    }

    public List<UUID> expected(UUID teamId, Assessment assessment) {
        return onTeam(teamId).stream().filter(a -> a.getRole() == assessment.role()).map(TeamReviewer::getReviewerId)
                .toList();
    }

    public List<Student> studentsOf(UUID teamId) {
        return students.stream().filter(s -> s.getTeamId().equals(teamId))
                .sorted(Comparator.comparing(Student::getLastName).thenComparing(Student::getFirstName)).toList();
    }

    public Optional<Grade> grade(UUID teamId, Assessment assessment, UUID reviewerId) {
        return grades.stream().filter(g -> g.getTeamId().equals(teamId) && g.getAssessment() == assessment
                && g.getReviewerId().equals(reviewerId)).findFirst();
    }

    public Optional<TeamAssessment> teamAssessment(UUID teamId, Assessment assessment) {
        return teamAssessments.stream().filter(t -> t.getTeamId().equals(teamId) && t.getAssessment() == assessment)
                .findFirst();
    }

    public Optional<Summary> evaluation(UUID teamId, Assessment assessment, UUID reviewerId) {
        return evaluations.stream().filter(e -> e.teamId().equals(teamId) && e.assessment() == assessment
                && e.reviewerId().equals(reviewerId)).findFirst();
    }

    public Instant due(Assessment assessment) {
        return dueDates.get(assessment);
    }

    public boolean pastDue(Assessment assessment) {
        Instant due = due(assessment);
        return due != null && now.isAfter(due);
    }

    public PersonView person(UUID reviewerId) {
        return PersonView.of(reviewers.get(reviewerId));
    }

    public List<TeamReviewerView> reviewerViews(UUID teamId) {
        return onTeam(teamId).stream().map(a -> new TeamReviewerView(person(a.getReviewerId()), a.getRole())).toList();
    }

    public Composition.Result composition(UUID teamId) {
        return Composition.check(onTeam(teamId).stream()
                .map(a -> new Composition.Assignment(a.getReviewerId(), a.getRole())).toList(), reviewers.keySet());
    }

    public ReviewerProgress progress(UUID teamId, Assessment assessment, UUID reviewerId) {
        Optional<Grade> grade = grade(teamId, assessment, reviewerId);
        Optional<Summary> evaluation = evaluation(teamId, assessment, reviewerId);
        EvaluationStatus status = grade.isPresent() ? EvaluationStatus.SUBMITTED
                : evaluation.map(e -> EvaluationStatus.valueOf(e.status())).orElse(EvaluationStatus.NOT_STARTED);
        return new ReviewerProgress(person(reviewerId), status,
                status != EvaluationStatus.SUBMITTED && pastDue(assessment),
                grade.map(g -> GradeMath.display(g.getScore())).orElse(null),
                grade.map(Grade::getSubmittedAt).orElse(null),
                grade.map(Grade::getEvaluationId).or(() -> evaluation.map(Summary::evaluationId)).orElse(null),
                evaluation.map(Summary::scoredCount).orElse(null),
                evaluation.map(Summary::criteriaCount).orElse(null));
    }

    public TeamAssessmentView assessmentView(UUID teamId, Assessment assessment) {
        List<UUID> expected = expected(teamId, assessment);
        List<ReviewerProgress> progress = expected.stream().map(r -> progress(teamId, assessment, r)).toList();
        List<Double> scores = expected.stream().map(r -> grade(teamId, assessment, r)).flatMap(Optional::stream)
                .map(Grade::getScore).toList();
        Optional<TeamAssessment> ta = teamAssessment(teamId, assessment);
        TeamAssessmentState state = ta.map(t -> t.isReleased() ? TeamAssessmentState.RELEASED : TeamAssessmentState.FINAL)
                .orElse(scores.isEmpty() ? TeamAssessmentState.NOT_STARTED : TeamAssessmentState.WAITING);
        return new TeamAssessmentView(assessment, assessment.weight(), assessment.role(), due(assessment), state,
                ta.map(t -> GradeMath.display(t.getGrade())).orElse(null), scores.size(), expected.size(),
                GradeMath.display(GradeMath.spread(scores).orElse(null)), ta.isEmpty() && pastDue(assessment),
                ta.map(TeamAssessment::getFinalizedAt).orElse(null), ta.map(TeamAssessment::getReleasedAt).orElse(null),
                progress);
    }

    public List<TeamAssessmentView> assessmentViews(UUID teamId) {
        return Arrays.stream(Assessment.values()).map(a -> assessmentView(teamId, a)).toList();
    }

    public FinalState finalState(Team team) {
        if (team.getFinalGrade() == null) {
            return FinalState.IN_PROGRESS;
        }
        boolean allReleased = Arrays.stream(Assessment.values())
                .allMatch(a -> teamAssessment(team.getId(), a).map(TeamAssessment::isReleased).orElse(false));
        return allReleased ? FinalState.VISIBLE : FinalState.COMPUTED;
    }

    public DefenseSlotView slotView(UUID teamId) {
        return slots.stream().filter(s -> s.getTeamId().equals(teamId)).findFirst().map(this::slotView).orElse(null);
    }

    public DefenseSlotView slotView(DefenseSlot slot) {
        Team team = team(slot.getTeamId()).orElseThrow();
        Set<UUID> jury = Set.copyOf(expected(team.getId(), Assessment.FINAL_PRESENTATION));
        List<ConflictView> conflicts = new ArrayList<>();
        for (DefenseSlot other : slots) {
            if (other == slot || !other.overlaps(slot)) {
                continue;
            }
            Team otherTeam = team(other.getTeamId()).orElseThrow();
            expected(otherTeam.getId(), Assessment.FINAL_PRESENTATION).stream().filter(jury::contains)
                    .forEach(r -> conflicts.add(new ConflictView(person(r), TeamRef.of(otherTeam))));
        }
        List<PersonView> juryViews = onTeam(team.getId()).stream().filter(a -> a.getRole() == ReviewerRole.JURY)
                .map(a -> person(a.getReviewerId())).toList();
        return new DefenseSlotView(TeamRef.of(team), slot.getStartsAt(), slot.getEndsAt(), slot.getRoom(), juryViews,
                conflicts);
    }

    public List<DefenseSlotView> slotViews() {
        return slots.stream().sorted(Comparator.comparing(DefenseSlot::getStartsAt)
                        .thenComparing(s -> team(s.getTeamId()).map(Team::getName).orElse("")))
                .map(this::slotView).toList();
    }

    /** Reviewers on two overlapping defenses: one entry per reviewer and start time. */
    public List<Views.ScheduleConflict> conflicts() {
        Map<String, Views.ScheduleConflict> found = new java.util.LinkedHashMap<>();
        for (DefenseSlotView slot : slotViews()) {
            for (ConflictView c : slot.conflicts()) {
                String key = c.reviewer().id() + "@" + slot.startsAt();
                found.compute(key, (k, existing) -> {
                    List<TeamRef> teamsInConflict = new ArrayList<>(existing == null ? List.of() : existing.teams());
                    for (TeamRef t : List.of(slot.team(), c.otherTeam())) {
                        if (teamsInConflict.stream().noneMatch(x -> x.id().equals(t.id()))) {
                            teamsInConflict.add(t);
                        }
                    }
                    return new Views.ScheduleConflict(c.reviewer(), slot.startsAt(), teamsInConflict);
                });
            }
        }
        return List.copyOf(found.values());
    }

    public Map<UUID, Long> teamCountByReviewer() {
        return assignments.stream().collect(Collectors.groupingBy(TeamReviewer::getReviewerId, Collectors.counting()));
    }

    public List<Team> teamsByName() {
        return teams.stream().sorted(Comparator.comparing(Team::getName, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    static boolean same(UUID a, UUID b) {
        return Objects.equals(a, b);
    }
}
