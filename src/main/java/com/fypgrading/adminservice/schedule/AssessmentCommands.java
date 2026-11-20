package com.fypgrading.adminservice.schedule;

import com.fypgrading.adminservice.common.demo.DemoSession;
import com.fypgrading.adminservice.common.demo.SeedIds;
import com.fypgrading.adminservice.common.security.CurrentUser;
import com.fypgrading.adminservice.common.web.ApiException;
import com.fypgrading.adminservice.domain.Assessment;
import com.fypgrading.adminservice.domain.AssessmentSchedule;
import com.fypgrading.adminservice.domain.AssessmentScheduleRepository;
import com.fypgrading.adminservice.domain.Student;
import com.fypgrading.adminservice.domain.StudentRepository;
import com.fypgrading.adminservice.domain.Team;
import com.fypgrading.adminservice.domain.TeamAssessment;
import com.fypgrading.adminservice.domain.TeamAssessmentRepository;
import com.fypgrading.adminservice.domain.TeamRepository;
import com.fypgrading.adminservice.events.Events;
import com.fypgrading.adminservice.views.Views.ReleaseResult;
import jakarta.validation.constraints.NotNull;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private final StudentRepository students;
    private final Events events;
    private final DueReminders reminders;
    private final Clock clock;

    public AssessmentCommands(
            AssessmentScheduleRepository schedules,
            TeamRepository teams,
            TeamAssessmentRepository teamAssessments,
            StudentRepository students,
            Events events,
            DueReminders reminders,
            Clock clock) {
        this.schedules = schedules;
        this.teams = teams;
        this.teamAssessments = teamAssessments;
        this.students = students;
        this.events = events;
        this.reminders = reminders;
        this.clock = clock;
    }

    public record AssessmentPatch(@NotNull Instant dueAt) {}

    /** Moves the due date for every team. Never blocks grading; see {@link DueReminders} for who is told. */
    @Transactional
    public void setDueDate(Assessment assessment, AssessmentPatch body) {
        AssessmentSchedule schedule = schedules
                .findByAssessment(assessment)
                .orElseGet(() -> schedules.save(new AssessmentSchedule(
                        SeedIds.of(DemoSession.require(), "schedule:" + assessment), assessment, body.dueAt())));
        schedule.setDueAt(body.dueAt());
        Instant now = clock.instant();
        // Within three days or past it: the reviewers still owing an evaluation and the coordinator are reminded.
        reminders.remind(assessment, body.dueAt(), now);
        String due = Dates.dayAndTime(body.dueAt());
        CurrentUser me = CurrentUser.require();
        events.activity(
                "SCHEDULE",
                me.id(),
                me.displayName(),
                "set the " + assessment.displayName() + " due date to " + due + ".",
                now,
                Events.Link.of("SCHEDULE", null, assessment));
    }

    /**
     * Releases the assessment to every team whose grade for it is final and not yet released. Teams that aren't
     * final are skipped and can be released by calling this again. Permanent.
     */
    @Transactional
    public ReleaseResult release(Assessment assessment) {
        Map<UUID, Team> byId = teams.findAll().stream().collect(Collectors.toMap(Team::getId, Function.identity()));
        Map<UUID, TeamAssessment> finals = teamAssessments.lockByAssessment(assessment).stream()
                .collect(Collectors.toMap(TeamAssessment::getTeamId, Function.identity()));
        Instant now = clock.instant();
        CurrentUser me = CurrentUser.require();
        List<UUID> released = new ArrayList<>();
        List<UUID> already = new ArrayList<>();
        List<UUID> notFinal = new ArrayList<>();
        for (Team team : byId.values().stream()
                .sorted(java.util.Comparator.comparing(Team::getName))
                .toList()) {
            TeamAssessment ta = finals.get(team.getId());
            if (ta == null) {
                notFinal.add(team.getId());
            } else if (ta.isReleased()) {
                already.add(team.getId());
            } else {
                ta.release(now, me.id());
                released.add(team.getId());
            }
        }
        if (released.isEmpty()) {
            throw ApiException.conflict(
                    "nothing-to-release",
                    "No team has a final " + assessment.displayName() + " grade waiting to be released.");
        }
        for (UUID teamId : released) {
            List<UUID> recipients = students.findByTeamIdOrderByLastNameAscFirstNameAsc(teamId).stream()
                    .map(Student::getUserId)
                    .filter(Objects::nonNull)
                    .toList();
            events.notify(
                    "released:" + teamId + ":" + assessment,
                    "RESULTS_RELEASED",
                    "Your " + assessment.displayName() + " results are available.",
                    now,
                    Events.Link.of("STUDENT_RESULTS", teamId, assessment),
                    recipients);
        }
        // One feed line per team, so each team's history (GET /api/activity?teamId=) shows its own release.
        for (UUID teamId : released) {
            events.activity(
                    "RELEASED",
                    me.id(),
                    me.displayName(),
                    "released " + assessment.displayName() + " results to Team "
                            + byId.get(teamId).getName() + ".",
                    now,
                    Events.Link.team(teamId, assessment));
        }
        return new ReleaseResult(assessment, now, released, already, notFinal);
    }
}
