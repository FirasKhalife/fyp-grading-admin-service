package com.fypgrading.adminservice.views;

import com.fypgrading.adminservice.clients.EvaluationClient;
import com.fypgrading.adminservice.common.demo.DemoSession;
import com.fypgrading.adminservice.common.demo.SeedClock;
import com.fypgrading.adminservice.common.web.ApiException;
import com.fypgrading.adminservice.domain.Assessment;
import com.fypgrading.adminservice.domain.AssessmentScheduleRepository;
import com.fypgrading.adminservice.domain.DefenseSlotRepository;
import com.fypgrading.adminservice.domain.DemoCopy;
import com.fypgrading.adminservice.domain.DemoCopyRepository;
import com.fypgrading.adminservice.domain.GradeRepository;
import com.fypgrading.adminservice.domain.Reviewer;
import com.fypgrading.adminservice.domain.ReviewerRepository;
import com.fypgrading.adminservice.domain.Student;
import com.fypgrading.adminservice.domain.StudentRepository;
import com.fypgrading.adminservice.domain.Team;
import com.fypgrading.adminservice.domain.TeamAssessment;
import com.fypgrading.adminservice.domain.TeamAssessmentRepository;
import com.fypgrading.adminservice.domain.TeamRepository;
import com.fypgrading.adminservice.domain.TeamReviewer;
import com.fypgrading.adminservice.domain.TeamReviewerRepository;
import com.fypgrading.adminservice.grading.GradeMath;
import com.fypgrading.adminservice.views.Views.AssessmentView;
import com.fypgrading.adminservice.views.Views.DueDate;
import com.fypgrading.adminservice.views.Views.EvaluationStatus;
import com.fypgrading.adminservice.views.Views.Kpis;
import com.fypgrading.adminservice.views.Views.MyAssignments;
import com.fypgrading.adminservice.views.Views.MySummary;
import com.fypgrading.adminservice.views.Views.MyTask;
import com.fypgrading.adminservice.views.Views.MyTeam;
import com.fypgrading.adminservice.views.Views.Overview;
import com.fypgrading.adminservice.views.Views.OverviewCell;
import com.fypgrading.adminservice.views.Views.OverviewRow;
import com.fypgrading.adminservice.views.Views.ReviewerProgress;
import com.fypgrading.adminservice.views.Views.Schedule;
import com.fypgrading.adminservice.views.Views.StudentHome;
import com.fypgrading.adminservice.views.Views.StudentTeam;
import com.fypgrading.adminservice.views.Views.StudentView;
import com.fypgrading.adminservice.views.Views.TeamAssessmentState;
import com.fypgrading.adminservice.views.Views.TeamAssessmentView;
import com.fypgrading.adminservice.views.Views.TeamDetail;
import com.fypgrading.adminservice.views.Views.TeamRef;
import com.fypgrading.adminservice.views.Views.TeamSummary;
import com.fypgrading.adminservice.views.Views.TimelineStep;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Builds the screens' views (P3, P5, P6, P8, P9, P11) from one {@link Snapshot} of the visitor's copy. */
@Service
public class ReadModel {

    private final TeamRepository teams;
    private final StudentRepository students;
    private final TeamReviewerRepository assignments;
    private final GradeRepository grades;
    private final TeamAssessmentRepository teamAssessments;
    private final DefenseSlotRepository slots;
    private final AssessmentScheduleRepository schedules;
    private final ReviewerRepository reviewers;
    private final DemoCopyRepository copies;
    private final EvaluationClient evaluations;
    private final Clock clock;
    private final double disagreementThreshold;

    public ReadModel(
            TeamRepository teams,
            StudentRepository students,
            TeamReviewerRepository assignments,
            GradeRepository grades,
            TeamAssessmentRepository teamAssessments,
            DefenseSlotRepository slots,
            AssessmentScheduleRepository schedules,
            ReviewerRepository reviewers,
            DemoCopyRepository copies,
            EvaluationClient evaluations,
            Clock clock,
            @org.springframework.beans.factory.annotation.Value("${assay.insights.disagreement-threshold:15}")
                    double disagreementThreshold) {
        this.teams = teams;
        this.students = students;
        this.assignments = assignments;
        this.grades = grades;
        this.teamAssessments = teamAssessments;
        this.slots = slots;
        this.schedules = schedules;
        this.reviewers = reviewers;
        this.copies = copies;
        this.evaluations = evaluations;
        this.clock = clock;
        this.disagreementThreshold = disagreementThreshold;
    }

    @Transactional(readOnly = true)
    public Snapshot snapshot(boolean withEvaluations) {
        Instant now = clock.instant();
        LocalDate anchor = copies.findById(DemoSession.require())
                .map(DemoCopy::getAnchorDate)
                .orElse(LocalDate.ofInstant(now, SeedClock.ZONE));
        Map<Assessment, Instant> due = new EnumMap<>(Assessment.class);
        schedules.findAll().forEach(s -> due.put(s.getAssessment(), s.getDueAt()));
        Map<UUID, Reviewer> people =
                reviewers.findAll().stream().collect(Collectors.toMap(Reviewer::getId, Function.identity()));
        return new Snapshot(
                now,
                anchor,
                teams.findAll(),
                students.findAll(),
                assignments.findAll(),
                grades.findAll(),
                teamAssessments.findAll(),
                slots.findAll(),
                due,
                people,
                withEvaluations ? evaluations.summaries() : List.of(),
                disagreementThreshold);
    }

    // ------------------------------------------------------------------ assessments (P5 columns, P9 due dates)

    public List<AssessmentView> assessments(Snapshot s) {
        return Arrays.stream(Assessment.values())
                .map(a -> {
                    List<TeamAssessment> forA = s.teamAssessments().stream()
                            .filter(t -> t.getAssessment() == a)
                            .toList();
                    return new AssessmentView(
                            a,
                            a.displayName(),
                            a.shortName(),
                            a.position(),
                            a.weight(),
                            true,
                            a.role(),
                            s.due(a),
                            s.teams().size(),
                            forA.size(),
                            (int) forA.stream()
                                    .filter(TeamAssessment::isReleased)
                                    .count());
                })
                .toList();
    }

    // ------------------------------------------------------------------ P5 overview

    public Overview overview() {
        Snapshot s = snapshot(true);
        List<OverviewRow> rows = s.teamsByName().stream()
                .map(t -> {
                    List<OverviewCell> cells = new ArrayList<>();
                    int pending = 0;
                    for (TeamAssessmentView v : s.assessmentViews(t.getId())) {
                        int drafts = (int) v.reviewers().stream()
                                .filter(r -> r.status() == EvaluationStatus.DRAFT)
                                .count();
                        boolean overdue = v.reviewers().stream().anyMatch(ReviewerProgress::overdue);
                        cells.add(new OverviewCell(
                                v.assessment(),
                                v.state(),
                                v.grade(),
                                v.submittedCount(),
                                v.expectedCount(),
                                drafts,
                                overdue,
                                v.spread(),
                                v.disagreement(),
                                v.releasedAt()));
                        pending += v.expectedCount() - v.submittedCount();
                    }
                    return new OverviewRow(
                            TeamRef.of(t),
                            cells,
                            GradeMath.display(t.getFinalGrade()),
                            s.finalState(t),
                            pending,
                            s.composition(t.getId()),
                            s.slotView(t.getId()));
                })
                .toList();

        int submitted = 0;
        int expected = 0;
        int overdue = 0;
        for (OverviewRow row : rows) {
            for (OverviewCell c : row.cells()) {
                submitted += c.submittedCount();
                expected += c.expectedCount();
            }
        }
        for (Team t : s.teams()) {
            for (TeamAssessmentView v : s.assessmentViews(t.getId())) {
                overdue += (int)
                        v.reviewers().stream().filter(ReviewerProgress::overdue).count();
            }
        }
        List<Double> finals = s.teams().stream()
                .map(Team::getFinalGrade)
                .filter(Objects::nonNull)
                .toList();
        int released = (int)
                s.teamAssessments().stream().filter(TeamAssessment::isReleased).count();
        Kpis kpis = new Kpis(
                submitted,
                expected,
                overdue,
                finals.size(),
                s.teams().size(),
                released,
                s.teamAssessments().size() - released,
                finals.isEmpty() ? null : GradeMath.display(GradeMath.average(finals)));
        return new Overview(kpis, assessments(s), rows, insights(s, rows, finals));
    }

    private Views.Insights insights(Snapshot s, List<OverviewRow> rows, List<Double> finals) {
        List<Views.GradeBand> bands = new ArrayList<>();
        for (int from = 0; from < 100; from += 10) {
            int lo = from;
            int hi = from + 10;
            int count = (int) finals.stream()
                    .filter(g -> g >= lo && (g < hi || (hi == 100 && g <= 100)))
                    .count();
            bands.add(new Views.GradeBand(lo, hi, count));
        }
        List<Views.Disagreement> disagreements = new ArrayList<>();
        for (OverviewRow row : rows) {
            for (OverviewCell c : row.cells()) {
                if (c.disagreement()) {
                    disagreements.add(
                            new Views.Disagreement(row.team(), c.assessment(), c.spread(), c.submittedCount()));
                }
            }
        }
        disagreements.sort(
                Comparator.comparingDouble(Views.Disagreement::spread).reversed());
        return new Views.Insights(s.disagreementThreshold(), bands, disagreements);
    }

    // ------------------------------------------------------------------ P6 / P8 teams

    public TeamDetail team(UUID teamId, UUID viewer, boolean admin) {
        Snapshot s = snapshot(true);
        Team team = s.team(teamId).orElseThrow(() -> ApiException.notFound("The team"));
        if (!admin && s.onTeam(teamId).stream().noneMatch(a -> a.getReviewerId().equals(viewer))) {
            throw ApiException.forbidden("not-assigned", "You're not a reviewer on this team.");
        }
        List<TeamAssessmentView> assessments = s.assessmentViews(teamId);
        if (!admin) {
            // Reviewers see each other's scores only once the team grade is final.
            assessments = assessments.stream()
                    .map(v -> v.state() == TeamAssessmentState.FINAL || v.state() == TeamAssessmentState.RELEASED
                            ? v
                            : hideOthers(v, viewer))
                    .toList();
        }
        return new TeamDetail(
                team.getId(),
                team.getName(),
                team.getProjectTitle(),
                team.getDescription(),
                s.studentsOf(teamId).stream().map(StudentView::of).toList(),
                s.reviewerViews(teamId),
                s.composition(teamId),
                s.slotView(teamId),
                assessments,
                GradeMath.display(team.getFinalGrade()),
                s.finalState(team));
    }

    public List<TeamSummary> teamSummaries() {
        Snapshot s = snapshot(false);
        return s.teamsByName().stream()
                .map(t -> new TeamSummary(
                        t.getId(),
                        t.getName(),
                        t.getProjectTitle(),
                        s.studentsOf(t.getId()).size(),
                        s.composition(t.getId()),
                        GradeMath.display(t.getFinalGrade()),
                        s.finalState(t)))
                .toList();
    }

    private static TeamAssessmentView hideOthers(TeamAssessmentView v, UUID viewer) {
        List<ReviewerProgress> reviewers = v.reviewers().stream()
                .map(r -> r.reviewer().id().equals(viewer)
                        ? r
                        : new ReviewerProgress(
                                r.reviewer(),
                                r.status(),
                                r.overdue(),
                                r.submittedLate(),
                                null,
                                r.submittedAt(),
                                null,
                                null,
                                null))
                .toList();
        return new TeamAssessmentView(
                v.assessment(),
                v.weight(),
                v.reviewerRole(),
                v.dueAt(),
                v.state(),
                v.grade(),
                v.submittedCount(),
                v.expectedCount(),
                null,
                false,
                v.overdue(),
                v.finalizedAt(),
                v.releasedAt(),
                reviewers);
    }

    // ------------------------------------------------------------------ P3 my teams

    public MyAssignments myAssignments(UUID me) {
        Snapshot s = snapshot(true);
        List<MyTeam> mine = new ArrayList<>();
        int expected = 0;
        int submitted = 0;
        int drafts = 0;
        int overdue = 0;
        for (TeamReviewer a : s.assignments().stream()
                .filter(x -> x.getReviewerId().equals(me))
                .toList()) {
            Team team = s.team(a.getTeamId()).orElseThrow();
            List<MyTask> tasks = new ArrayList<>();
            for (Assessment assessment : Assessment.forRole(a.getRole())) {
                ReviewerProgress p = s.progress(team.getId(), assessment, me);
                var evaluation = s.evaluation(team.getId(), assessment, me);
                tasks.add(new MyTask(
                        assessment,
                        assessment.displayName(),
                        assessment.weight(),
                        s.due(assessment),
                        p.status(),
                        p.overdue(),
                        p.submittedLate(),
                        p.score(),
                        evaluation.map(e -> e.scoredCount()).orElse(0),
                        evaluation.map(e -> e.criteriaCount()).orElse(0),
                        p.evaluationId(),
                        evaluation.map(e -> e.updatedAt()).orElse(p.submittedAt()),
                        p.submittedAt()));
                expected++;
                submitted += p.status() == EvaluationStatus.SUBMITTED ? 1 : 0;
                drafts += p.status() == EvaluationStatus.DRAFT ? 1 : 0;
                overdue += p.overdue() ? 1 : 0;
            }
            mine.add(new MyTeam(
                    TeamRef.of(team), a.getRole(), s.reviewerViews(team.getId()), s.slotView(team.getId()), tasks));
        }
        mine.sort(Comparator.comparing((MyTeam t) -> t.tasks().stream()
                        .filter(x -> x.status() != EvaluationStatus.SUBMITTED)
                        .map(MyTask::dueAt)
                        .filter(Objects::nonNull)
                        .min(Instant::compareTo)
                        .orElse(Instant.MAX))
                .thenComparing(t -> t.team().name()));
        return new MyAssignments(new MySummary(expected, submitted, drafts, overdue), mine);
    }

    // ------------------------------------------------------------------ P9 schedule

    public Schedule schedule(UUID viewer, boolean admin, boolean student) {
        Snapshot s = snapshot(false);
        Set<UUID> visibleTeams;
        if (admin) {
            visibleTeams = s.teams().stream().map(Team::getId).collect(Collectors.toSet());
        } else if (student) {
            visibleTeams = s.students().stream()
                    .filter(st -> viewer.equals(st.getUserId()))
                    .map(Student::getTeamId)
                    .collect(Collectors.toSet());
        } else {
            visibleTeams = s.assignments().stream()
                    .filter(a -> a.getReviewerId().equals(viewer))
                    .map(TeamReviewer::getTeamId)
                    .collect(Collectors.toSet());
        }
        var slots = s.slotViews().stream()
                .filter(v -> visibleTeams.contains(v.team().id()))
                .toList();
        var conflicts = admin
                ? s.conflicts()
                : s.conflicts().stream()
                        .filter(c -> c.teams().stream().anyMatch(t -> visibleTeams.contains(t.id())))
                        .toList();
        List<LocalDate> days =
                List.of(s.anchor().minusDays(1), s.anchor(), s.anchor().plusDays(1));
        List<DueDate> due = Arrays.stream(Assessment.values())
                .map(a -> new DueDate(a, a.displayName(), s.due(a)))
                .toList();
        return new Schedule(SeedClock.ZONE.getId(), days, due, slots, conflicts);
    }

    // ------------------------------------------------------------------ P11 student home

    public StudentHome studentHome(UUID userId) {
        Snapshot s = snapshot(true);
        Student me = s.students().stream()
                .filter(st -> userId.equals(st.getUserId()))
                .findFirst()
                .orElseThrow(() -> ApiException.notFound("Your team"));
        Team team = s.team(me.getTeamId()).orElseThrow(() -> ApiException.notFound("Your team"));
        List<TimelineStep> timeline = new ArrayList<>();
        boolean allReleased = true;
        for (Assessment a : Assessment.values()) {
            TeamAssessmentView v = s.assessmentView(team.getId(), a);
            boolean released = v.state() == TeamAssessmentState.RELEASED;
            allReleased &= released;
            String status = v.state() == TeamAssessmentState.FINAL || released
                    ? "GRADED"
                    : v.submittedCount() > 0 || s.pastDue(a) ? "BEING_GRADED" : "UPCOMING";
            timeline.add(new TimelineStep(
                    a,
                    a.displayName(),
                    a.weight(),
                    v.dueAt(),
                    status,
                    released,
                    v.releasedAt(),
                    released ? v.grade() : null));
        }
        boolean finalVisible = allReleased && team.getFinalGrade() != null;
        return new StudentHome(
                new StudentTeam(team.getId(), team.getName(), team.getProjectTitle(), team.getDescription()),
                StudentView.of(me),
                s.studentsOf(team.getId()).stream()
                        .filter(st -> !st.getId().equals(me.getId()))
                        .map(StudentView::of)
                        .toList(),
                s.reviewerViews(team.getId()),
                s.slotView(team.getId()),
                timeline,
                finalVisible,
                finalVisible ? GradeMath.display(team.getFinalGrade()) : null);
    }
}
