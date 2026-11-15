package com.fypgrading.adminservice.schedule;

import com.fypgrading.adminservice.common.demo.DemoSession;
import com.fypgrading.adminservice.common.demo.SeedIds;
import com.fypgrading.adminservice.common.security.CurrentUser;
import com.fypgrading.adminservice.common.web.ApiException;
import com.fypgrading.adminservice.domain.Assessment;
import com.fypgrading.adminservice.domain.AssessmentSchedule;
import com.fypgrading.adminservice.domain.AssessmentScheduleRepository;
import com.fypgrading.adminservice.domain.GradeRepository;
import com.fypgrading.adminservice.domain.Student;
import com.fypgrading.adminservice.domain.StudentRepository;
import com.fypgrading.adminservice.domain.Team;
import com.fypgrading.adminservice.domain.TeamAssessment;
import com.fypgrading.adminservice.domain.TeamAssessmentRepository;
import com.fypgrading.adminservice.domain.TeamRepository;
import com.fypgrading.adminservice.domain.TeamReviewerRepository;
import com.fypgrading.adminservice.events.Events;
import com.fypgrading.adminservice.views.Views.ReleaseResult;
import jakarta.validation.constraints.NotNull;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Due dates and the release of results to students, one assessment at a time. */
@Service
public class AssessmentCommands {

    private final AssessmentScheduleRepository schedules;
    private final TeamRepository teams;
    private final TeamAssessmentRepository teamAssessments;
    private final TeamReviewerRepository assignments;
    private final GradeRepository grades;
    private final StudentRepository students;
    private final Events events;
    private final Clock clock;

    public AssessmentCommands(AssessmentScheduleRepository schedules, TeamRepository teams,
            TeamAssessmentRepository teamAssessments, TeamReviewerRepository assignments, GradeRepository grades,
            StudentRepository students, Events events, Clock clock) {
        this.schedules = schedules;
        this.teams = teams;
        this.teamAssessments = teamAssessments;
        this.assignments = assignments;
        this.grades = grades;
        this.students = students;
        this.events = events;
        this.clock = clock;
    }

    public record AssessmentPatch(@NotNull Instant dueAt) {
    }

    /** Moves the due date for every team. Never blocks grading; reviewers still owing an evaluation are told. */
    @Transactional
    public void setDueDate(Assessment assessment, AssessmentPatch body) {
        AssessmentSchedule schedule = schedules.findByAssessment(assessment).orElseGet(() -> schedules.save(
                new AssessmentSchedule(SeedIds.of(DemoSession.require(), "schedule:" + assessment), assessment,
                        body.dueAt())));
        schedule.setDueAt(body.dueAt());
        Instant now = clock.instant();
        Set<UUID> pending = assignments.findAll().stream()
                .filter(a -> a.getRole() == assessment.role())
                .filter(a -> grades.findByTeamIdAndAssessmentAndReviewerId(a.getTeamId(), assessment, a.getReviewerId())
                        .isEmpty())
                .map(a -> a.getReviewerId())
                .collect(Collectors.toSet());
        String due = Dates.dayAndTime(body.dueAt());
        events.notify("due:" + assessment + ":" + body.dueAt() + ":" + now, now.isAfter(body.dueAt()) ? "OVERDUE" : "DUE_SOON",
                "The " + assessment.displayName() + " is now due " + due + ".", now,
                Events.Link.of("MY_TEAMS", null, assessment), pending);
        CurrentUser me = CurrentUser.require();
        events.activity("SCHEDULE", me.id(), me.displayName(),
                "set the " + assessment.displayName() + " due date to " + due + ".", now,
                Events.Link.of("SCHEDULE", null, assessment));
    }

    /**
     * Releases the assessment to every team whose grade for it is final and not yet released. Teams that aren't
     * final are skipped and can be released by calling this again. Permanent.
     */
    @Transactional
    public ReleaseResult release(Assessment assessment) {
        Map<UUID, Team> byId = teams.findAll().stream().collect(Collectors.toMap(Team::getId, Function.identity()));
        Map<UUID, TeamAssessment> finals = teamAssessments.findByAssessment(assessment).stream()
                .collect(Collectors.toMap(TeamAssessment::getTeamId, Function.identity()));
        Instant now = clock.instant();
        List<UUID> released = new ArrayList<>();
        List<UUID> already = new ArrayList<>();
        List<UUID> notFinal = new ArrayList<>();
        for (Team team : byId.values().stream().sorted(java.util.Comparator.comparing(Team::getName)).toList()) {
            TeamAssessment ta = finals.get(team.getId());
            if (ta == null) {
                notFinal.add(team.getId());
            } else if (ta.isReleased()) {
                already.add(team.getId());
            } else {
                ta.release(now);
                released.add(team.getId());
            }
        }
        if (released.isEmpty()) {
            throw ApiException.conflict("nothing-to-release", "No team has a final " + assessment.displayName()
                    + " grade waiting to be released.");
        }
        for (UUID teamId : released) {
            List<UUID> recipients = students.findByTeamIdOrderByLastNameAscFirstNameAsc(teamId).stream()
                    .map(Student::getUserId).filter(Objects::nonNull).toList();
            events.notify("released:" + teamId + ":" + assessment, "RESULTS_RELEASED",
                    "Your " + assessment.displayName() + " results are available.", now,
                    Events.Link.of("STUDENT_RESULTS", teamId, assessment), recipients);
        }
        CurrentUser me = CurrentUser.require();
        String names = released.stream().map(id -> "Team " + byId.get(id).getName()).collect(Collectors.joining(", "));
        events.activity("RELEASED", me.id(), me.displayName(),
                "released " + assessment.displayName() + " results to " + names + ".", now,
                Events.Link.of("OVERVIEW", null, assessment));
        return new ReleaseResult(assessment, now, released, already, notFinal);
    }
}
