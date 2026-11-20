package com.fypgrading.adminservice.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fypgrading.adminservice.domain.Assessment;
import com.fypgrading.adminservice.domain.AssessmentScheduleRepository;
import com.fypgrading.adminservice.domain.Grade;
import com.fypgrading.adminservice.domain.GradeRepository;
import com.fypgrading.adminservice.domain.Reviewer;
import com.fypgrading.adminservice.domain.ReviewerRepository;
import com.fypgrading.adminservice.domain.ReviewerRole;
import com.fypgrading.adminservice.domain.Team;
import com.fypgrading.adminservice.domain.TeamRepository;
import com.fypgrading.adminservice.domain.TeamReviewer;
import com.fypgrading.adminservice.domain.TeamReviewerRepository;
import com.fypgrading.adminservice.events.Events;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class DueRemindersTest {

    static final Instant NOW = Instant.parse("2026-06-10T08:00:00Z"); // 10:00 in Paris
    static final UUID BETA = UUID.randomUUID();
    static final UUID RANIA = UUID.randomUUID();
    static final UUID JANE = UUID.randomUUID();
    static final UUID JOHN = UUID.randomUUID();
    static final UUID EMILY = UUID.randomUUID();

    TeamReviewerRepository assignments = mock(TeamReviewerRepository.class);
    GradeRepository grades = mock(GradeRepository.class);
    TeamRepository teams = mock(TeamRepository.class);
    ReviewerRepository reviewers = mock(ReviewerRepository.class);
    Events events = mock(Events.class);
    DueReminders reminders = new DueReminders(
            mock(AssessmentScheduleRepository.class),
            teams,
            assignments,
            grades,
            reviewers,
            events,
            Clock.fixed(NOW, ZoneOffset.UTC));

    @BeforeEach
    void beta() {
        when(teams.findAll()).thenReturn(List.of(new Team(BETA, "Beta", "Project", null)));
        when(reviewers.findAll())
                .thenReturn(List.of(
                        new Reviewer(RANIA, "Rania", "Haddad", "r@example.edu", true),
                        new Reviewer(JANE, "Jane", "Smith", "j@example.edu", false)));
        when(assignments.findAll())
                .thenReturn(List.of(
                        new TeamReviewer(UUID.randomUUID(), BETA, JANE, ReviewerRole.JURY),
                        new TeamReviewer(UUID.randomUUID(), BETA, JOHN, ReviewerRole.JURY),
                        new TeamReviewer(UUID.randomUUID(), BETA, EMILY, ReviewerRole.ADVISOR)));
        when(grades.findByTeamIdAndAssessmentAndReviewerId(any(), any(), any())).thenReturn(Optional.empty());
        // John has handed in his final report.
        when(grades.findByTeamIdAndAssessmentAndReviewerId(BETA, Assessment.FINAL_REPORT, JOHN))
                .thenReturn(Optional.of(mock(Grade.class)));
    }

    @Test
    void dueWithinThreeDaysRemindsEachPendingReviewerAndSummarisesForTheCoordinator() {
        Instant dueAt = Instant.parse("2026-06-12T21:59:00Z"); // Fri 12 Jun, 23:59 in Paris
        reminders.remind(Assessment.FINAL_REPORT, dueAt, NOW);

        String key = "reminder:FINAL_REPORT:" + dueAt + ":2026-06-10";
        verify(events)
                .notify(
                        eq(key + ":" + BETA),
                        eq("DUE_SOON"),
                        eq("Final report for Team Beta is due in 2 days."),
                        eq(NOW),
                        eq(Events.Link.of("EVALUATION", BETA, Assessment.FINAL_REPORT)),
                        eq(List.of(JANE)));
        verify(events)
                .notify(
                        eq(key),
                        eq("DUE_SOON"),
                        eq("Final report is due in 2 days: 1 evaluation still open."),
                        eq(NOW),
                        eq(Events.Link.of("OVERVIEW", null, null)),
                        eq(List.of(RANIA)));
        verify(events, times(2)).notify(anyString(), anyString(), anyString(), any(), any(), any());
    }

    @Test
    void anOverdueAssessmentRemindsTheReviewerStillOwingIt() {
        reminders.remind(Assessment.ADVISOR_ASSESSMENT, Instant.parse("2026-06-05T21:59:00Z"), NOW);

        verify(events)
                .notify(
                        anyString(),
                        eq("OVERDUE"),
                        eq("Advisor assessment for Team Beta is overdue."),
                        eq(NOW),
                        any(),
                        eq(List.of(EMILY)));
        verify(events)
                .notify(
                        anyString(),
                        eq("OVERDUE"),
                        eq("Advisor assessment is overdue: 1 evaluation still open."),
                        eq(NOW),
                        any(),
                        eq(List.of(RANIA)));
    }

    @Test
    void nothingWhenTheDueDateIsMoreThanThreeDaysAway() {
        reminders.remind(Assessment.FINAL_REPORT, Instant.parse("2026-06-14T08:00:00Z"), NOW);
        verifyNoInteractions(events);
    }

    @Test
    void theSameDayAndDueDateGiveTheSameKeysSoNotificationServiceKeepsOneOfEach() {
        Instant dueAt = Instant.parse("2026-06-11T21:59:00Z");
        reminders.remind(Assessment.FINAL_REPORT, dueAt, NOW);
        reminders.remind(Assessment.FINAL_REPORT, dueAt, NOW.plusSeconds(3600));
        reminders.remind(Assessment.FINAL_REPORT, dueAt, Instant.parse("2026-06-11T08:00:00Z"));

        ArgumentCaptor<String> keys = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> texts = ArgumentCaptor.forClass(String.class);
        verify(events, times(6)).notify(keys.capture(), anyString(), texts.capture(), any(), any(), any());
        assertThat(keys.getAllValues().subList(0, 2))
                .isEqualTo(keys.getAllValues().subList(2, 4));
        assertThat(keys.getAllValues().subList(4, 6))
                .doesNotContainAnyElementsOf(keys.getAllValues().subList(0, 2));
        assertThat(texts.getAllValues().getFirst()).isEqualTo("Final report for Team Beta is due tomorrow.");
        assertThat(texts.getAllValues().get(4)).isEqualTo("Final report for Team Beta is due today.");
    }
}
