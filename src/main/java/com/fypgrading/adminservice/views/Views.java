package com.fypgrading.adminservice.views;

import com.fypgrading.adminservice.domain.Assessment;
import com.fypgrading.adminservice.domain.Reviewer;
import com.fypgrading.adminservice.domain.ReviewerRole;
import com.fypgrading.adminservice.domain.Student;
import com.fypgrading.adminservice.domain.Team;
import com.fypgrading.adminservice.teams.Composition;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Response bodies, one record per schema in contract/openapi.yaml. */
public final class Views {

    private Views() {
    }

    public enum EvaluationStatus { NOT_STARTED, DRAFT, SUBMITTED }

    public enum TeamAssessmentState { NOT_STARTED, WAITING, FINAL, RELEASED }

    public enum FinalState { IN_PROGRESS, COMPUTED, VISIBLE }

    public record PageMeta(int size, int number, long totalElements, int totalPages) {
    }

    public record PageView<T>(List<T> content, PageMeta page) {

        public static <T> PageView<T> of(List<T> all, int page, int size) {
            int from = Math.min(page * size, all.size());
            int to = Math.min(from + size, all.size());
            int pages = (all.size() + size - 1) / size;
            return new PageView<>(all.subList(from, to), new PageMeta(size, page, all.size(), pages));
        }
    }

    public record PersonView(UUID id, String firstName, String lastName, String displayName, String initials,
                             String email) {

        public static PersonView of(UUID id, String first, String last, String email) {
            String initials = (first.isEmpty() ? "" : first.substring(0, 1)) + (last.isEmpty() ? "" : last.substring(0, 1));
            return new PersonView(id, first, last, first + " " + last, initials.toUpperCase(), email);
        }

        public static PersonView of(Reviewer r) {
            return of(r.getId(), r.getFirstName(), r.getLastName(), r.getEmail());
        }
    }

    public record ReviewerView(UUID id, String firstName, String lastName, String displayName, String initials,
                               String email, boolean coordinator, long teamCount) {
    }

    public record StudentView(UUID id, String firstName, String lastName, String displayName, String initials,
                              String email, UUID teamId) {

        public static StudentView of(Student s) {
            PersonView p = PersonView.of(s.getId(), s.getFirstName(), s.getLastName(), s.getEmail());
            return new StudentView(p.id(), p.firstName(), p.lastName(), p.displayName(), p.initials(), p.email(),
                    s.getTeamId());
        }
    }

    public record TeamRef(UUID id, String name, String projectTitle) {

        public static TeamRef of(Team t) {
            return new TeamRef(t.getId(), t.getName(), t.getProjectTitle());
        }
    }

    public record TeamReviewerView(PersonView reviewer, ReviewerRole role) {
    }

    public record TeamReviewersView(List<TeamReviewerView> reviewers, Composition.Result composition) {
    }

    public record ConflictView(PersonView reviewer, TeamRef otherTeam) {
    }

    public record DefenseSlotView(TeamRef team, Instant startsAt, Instant endsAt, String room, List<PersonView> jury,
                                  List<ConflictView> conflicts) {
    }

    public record AssessmentView(Assessment code, String name, String shortName, int position, int weight,
                                 boolean required, ReviewerRole reviewerRole, Instant dueAt, int teamCount,
                                 int finalTeamCount, int releasedTeamCount) {
    }

    public record ReviewerProgress(PersonView reviewer, EvaluationStatus status, boolean overdue, boolean submittedLate,
                                   Double score,
                                   Instant submittedAt, UUID evaluationId, Integer scoredCount, Integer criteriaCount) {
    }

    public record TeamAssessmentView(Assessment assessment, int weight, ReviewerRole reviewerRole, Instant dueAt,
                                     TeamAssessmentState state, Double grade, int submittedCount, int expectedCount,
                                     Double spread, boolean overdue, Instant finalizedAt, Instant releasedAt,
                                     List<ReviewerProgress> reviewers) {
    }

    public record TeamSummary(UUID id, String name, String projectTitle, long studentCount,
                              Composition.Result composition, Double finalGrade, FinalState finalState) {
    }

    public record TeamDetail(UUID id, String name, String projectTitle, String description, List<StudentView> students,
                             List<TeamReviewerView> reviewers, Composition.Result composition,
                             DefenseSlotView defenseSlot, List<TeamAssessmentView> assessments, Double finalGrade,
                             FinalState finalState) {
    }

    public record MyTask(Assessment assessment, String name, int weight, Instant dueAt, EvaluationStatus status,
                         boolean overdue, boolean submittedLate, Double score, int scoredCount, int criteriaCount, UUID evaluationId,
                         Instant updatedAt, Instant submittedAt) {
    }

    public record MyTeam(TeamRef team, ReviewerRole role, List<TeamReviewerView> reviewers,
                         DefenseSlotView defenseSlot, List<MyTask> tasks) {
    }

    public record MySummary(int expected, int submitted, int drafts, int overdue) {
    }

    public record MyAssignments(MySummary summary, List<MyTeam> teams) {
    }

    public record OverviewCell(Assessment assessment, TeamAssessmentState state, Double grade, int submittedCount,
                               int expectedCount, int draftCount, boolean overdue, Double spread, Instant releasedAt) {
    }

    public record OverviewRow(TeamRef team, List<OverviewCell> cells, Double finalGrade, FinalState finalState,
                              int pendingEvaluations, Composition.Result composition, DefenseSlotView defenseSlot) {
    }

    public record Kpis(int evaluationsSubmitted, int evaluationsExpected, int overdue, int finalGradesComputed,
                       int teamCount, int resultsReleased, int resultsReleasable, Double averageFinalGrade) {
    }

    public record Overview(Kpis kpis, List<AssessmentView> assessments, List<OverviewRow> teams) {
    }

    public record DueDate(Assessment assessment, String name, Instant dueAt) {
    }

    public record ScheduleConflict(PersonView reviewer, Instant startsAt, List<TeamRef> teams) {
    }

    public record Schedule(String timeZone, List<LocalDate> defenseDays, List<DueDate> dueDates,
                           List<DefenseSlotView> slots, List<ScheduleConflict> conflicts) {
    }

    public record StudentTeam(UUID id, String name, String projectTitle, String description) {
    }

    public record TimelineStep(Assessment assessment, String name, int weight, Instant dueAt, String status,
                               boolean released, Instant releasedAt, Double grade) {
    }

    public record StudentHome(StudentTeam team, StudentView me, List<StudentView> teammates,
                              List<TeamReviewerView> reviewers, DefenseSlotView defenseSlot,
                              List<TimelineStep> timeline, boolean finalGradeVisible, Double finalGrade) {
    }

    public record ReleasedReviewer(PersonView reviewer, Double score, String overallComment) {
    }

    public record ReleasedLevel(UUID reviewerId, int level, String comment) {
    }

    public record ReleasedCriterion(UUID criterionId, String name, int weight, List<ReleasedLevel> levels) {
    }

    public record ReleasedResults(Assessment assessment, String name, int weight, Double grade, Instant releasedAt,
                                  List<ReleasedReviewer> reviewers, List<ReleasedCriterion> criteria) {
    }

    public record ReleaseResult(Assessment assessment, Instant releasedAt, List<UUID> releasedTeamIds,
                                List<UUID> alreadyReleasedTeamIds, List<UUID> notFinalTeamIds) {
    }

    public record Me(UUID id, String firstName, String lastName, String displayName, String initials, String email,
                     List<String> roles, String personaKey, UUID studentTeamId) {
    }

    public record Persona(String key, String username, String displayName, String roleLabel, List<String> roles,
                          List<String> tryThings) {
    }

    public record DemoSessionView(UUID id, Instant createdAt, Instant resetsAt, LocalDate today, String timeZone) {
    }

    /** Internal, for evaluation-service: may this reviewer grade this assessment for this team? */
    public record AssignmentCheck(UUID teamId, String teamName, Assessment assessment, ReviewerRole requiredRole,
                                  boolean assigned, InternalPerson reviewer, Instant dueAt,
                                  boolean teamAssessmentFinal, int submittedCount, int expectedCount) {
    }

    public record InternalPerson(UUID id, String firstName, String lastName) {
    }
}
