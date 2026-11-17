package com.fypgrading.adminservice.contracts;

import static org.assertj.core.api.Assertions.assertThat;

import com.fypgrading.adminservice.clients.EvaluationClient;
import com.fypgrading.adminservice.domain.Assessment;
import com.fypgrading.adminservice.domain.ReviewerRole;
import com.fypgrading.adminservice.events.Events;
import com.fypgrading.adminservice.grading.GradeIntake;
import com.fypgrading.adminservice.views.Views;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** The payloads admin-service exchanges with evaluation-service and notification-service. */
class PayloadContractsTest {

    static final UUID ID = UUID.randomUUID();
    static final Instant AT = Instant.parse("2026-06-10T08:00:00Z");

    @Test
    void producesTheAssignmentCheckEvaluationServiceReads() {
        Contracts.assertProducesExactly(
                "assignment-check",
                new Views.AssignmentCheck(
                        ID,
                        "Beta",
                        Assessment.FINAL_REPORT,
                        ReviewerRole.JURY,
                        true,
                        new Views.InternalPerson(ID, "Jane", "Smith"),
                        AT,
                        false,
                        2,
                        3));
    }

    @Test
    void producesTheGradeReceipt() {
        Contracts.assertProducesExactly("grade-receipt", new GradeIntake.Receipt(3, 3));
    }

    @Test
    void producesTheEventsNotificationServiceReads() {
        Events.Link link = Events.Link.team(ID, Assessment.FINAL_REPORT);
        Contracts.assertProducesExactly(
                "activity-event",
                new Events.ActivityEvent(
                        "e", ID.toString(), "SUBMITTED", ID.toString(), "Jane Smith", "submitted", AT, link));
        Contracts.assertProducesExactly(
                "notification-event",
                new Events.NotificationEvent(
                        "k", ID.toString(), "GRADE_FINAL", "final", AT, link, List.of(ID.toString())));
    }

    @Test
    void readsWhatEvaluationServiceSends() {
        GradeIntake.GradeSubmission grade = Contracts.read("grade-submission", GradeIntake.GradeSubmission.class);
        assertThat(grade.score()).isEqualTo(72.5);
        assertThat(grade.assessment()).isEqualTo(Assessment.FINAL_REPORT);
        assertThat(grade.evaluationId()).isNotNull();

        EvaluationClient.Summary summary = Contracts.read("evaluation-summary", EvaluationClient.Summary.class);
        assertThat(summary.status()).isEqualTo("DRAFT");
        assertThat(summary.scoredCount()).isEqualTo(8);

        EvaluationClient.Submitted submitted = Contracts.read("submitted-evaluation", EvaluationClient.Submitted.class);
        assertThat(submitted.reviewer().firstName()).isEqualTo("Jane");
        assertThat(submitted.criteria().getFirst().level()).isEqualTo(4);
        assertThat(submitted.criteria().getFirst().comment()).isNotBlank();
    }
}
