package com.fypgrading.adminservice.schedule;

import com.fypgrading.adminservice.common.demo.SeedClock;
import com.fypgrading.adminservice.domain.Assessment;
import com.fypgrading.adminservice.domain.AssessmentSchedule;
import com.fypgrading.adminservice.domain.AssessmentScheduleRepository;
import com.fypgrading.adminservice.domain.GradeRepository;
import com.fypgrading.adminservice.domain.Reviewer;
import com.fypgrading.adminservice.domain.ReviewerRepository;
import com.fypgrading.adminservice.domain.Team;
import com.fypgrading.adminservice.domain.TeamRepository;
import com.fypgrading.adminservice.domain.TeamReviewer;
import com.fypgrading.adminservice.domain.TeamReviewerRepository;
import com.fypgrading.adminservice.events.Events;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Brief 4.5 reminders: an assessment due within {@value #WINDOW_DAYS} days, or overdue, tells each reviewer who still
 * owes an evaluation for it (one notification per team), and sends the coordinator one summary per assessment.
 *
 * <p><b>When they run.</b> A demo copy never outlives its day: every copy is purged at midnight (Paris) and built
 * again from the seed on the next visit. So the "daily reminder run" is the moment a copy is created, at that copy's
 * "today"; there is no scheduler. They run again when the coordinator moves a due date.
 *
 * <p><b>Never twice.</b> The event key holds the assessment, the due date in force and the day, and
 * notification-service keeps one notification per (copy, event key, recipient). Running this again the same day
 * sends nothing new; a moved due date gets reminders of its own.
 */
@Service
public class DueReminders {

    static final int WINDOW_DAYS = 3;

    private final AssessmentScheduleRepository schedules;
    private final TeamRepository teams;
    private final TeamReviewerRepository assignments;
    private final GradeRepository grades;
    private final ReviewerRepository reviewers;
    private final Events events;
    private final Clock clock;

    public DueReminders(
            AssessmentScheduleRepository schedules,
            TeamRepository teams,
            TeamReviewerRepository assignments,
            GradeRepository grades,
            ReviewerRepository reviewers,
            Events events,
            Clock clock) {
        this.schedules = schedules;
        this.teams = teams;
        this.assignments = assignments;
        this.grades = grades;
        this.reviewers = reviewers;
        this.events = events;
        this.clock = clock;
    }

    /** Every assessment of the current demo copy, as of now. Call inside the copy's session and a transaction. */
    public void remindAll() {
        Instant now = clock.instant();
        for (AssessmentSchedule schedule : schedules.findAll()) {
            remind(schedule.getAssessment(), schedule.getDueAt(), now);
        }
    }

    /** One assessment with this due date, as of {@code now}. Nothing if it's more than the window away. */
    public void remind(Assessment assessment, Instant dueAt, Instant now) {
        LocalDate today = LocalDate.ofInstant(now, SeedClock.ZONE);
        long days = ChronoUnit.DAYS.between(today, LocalDate.ofInstant(dueAt, SeedClock.ZONE));
        boolean overdue = now.isAfter(dueAt);
        if (!overdue && days > WINDOW_DAYS) {
            return;
        }
        List<TeamReviewer> pending = assignments.findAll().stream()
                .filter(a -> a.getRole() == assessment.role())
                .filter(a -> grades.findByTeamIdAndAssessmentAndReviewerId(a.getTeamId(), assessment, a.getReviewerId())
                        .isEmpty())
                .toList();
        if (pending.isEmpty()) {
            return;
        }
        String type = overdue ? "OVERDUE" : "DUE_SOON";
        String state = overdue ? "is overdue" : "is due " + inDays(days);
        String key = "reminder:" + assessment + ":" + dueAt + ":" + today;
        Map<UUID, String> teamNames = teams.findAll().stream().collect(Collectors.toMap(Team::getId, Team::getName));
        for (TeamReviewer a : pending) {
            events.notify(
                    key + ":" + a.getTeamId(),
                    type,
                    assessment.displayName() + " for Team " + teamNames.get(a.getTeamId()) + " " + state + ".",
                    now,
                    Events.Link.of("EVALUATION", a.getTeamId(), assessment),
                    List.of(a.getReviewerId()));
        }
        List<UUID> coordinators = reviewers.findAll().stream()
                .filter(Reviewer::isCoordinator)
                .map(Reviewer::getId)
                .toList();
        events.notify(
                key,
                type,
                assessment.displayName() + " " + state + ": " + pending.size()
                        + (pending.size() == 1 ? " evaluation" : " evaluations") + " still open.",
                now,
                Events.Link.of("OVERVIEW", null, null),
                coordinators);
    }

    static String inDays(long days) {
        if (days <= 0) {
            return "today";
        }
        return days == 1 ? "tomorrow" : "in " + days + " days";
    }
}
