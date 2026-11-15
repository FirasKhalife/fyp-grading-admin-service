package com.fypgrading.adminservice.events;

import com.fypgrading.adminservice.common.demo.DemoSession;
import com.fypgrading.adminservice.domain.Assessment;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Publishes to RabbitMQ once the transaction commits:
 * <ul>
 *   <li>{@code assay.grades}: a grade arrived; consumed by this service to aggregate (see GradeAggregator).</li>
 *   <li>{@code assay.notifications}: personal notifications with their recipients resolved.</li>
 *   <li>{@code assay.activity}: the coordinator's feed.</li>
 * </ul>
 */
@Component
public class Events {

    private static final Logger log = LoggerFactory.getLogger(Events.class);

    private final StreamBridge bridge;

    public Events(StreamBridge bridge) {
        this.bridge = bridge;
    }

    public void gradeSubmitted(UUID teamId, Assessment assessment) {
        GradeSubmitted event = new GradeSubmitted(DemoSession.require().toString(), teamId.toString(), assessment);
        afterCommit(() -> send("grades-out-0", event));
    }

    public void notify(String eventKey, String type, String text, Instant at, Link link, Collection<UUID> recipients) {
        if (recipients.isEmpty()) {
            return;
        }
        NotificationEvent event = new NotificationEvent(eventKey, DemoSession.require().toString(), type, text, at, link,
                recipients.stream().distinct().map(UUID::toString).toList());
        afterCommit(() -> send("notifications-out-0", event));
    }

    public void activity(String kind, UUID actorId, String actorName, String text, Instant at, Link link) {
        ActivityEvent event = new ActivityEvent(UUID.randomUUID().toString(), DemoSession.require().toString(), kind,
                actorId == null ? null : actorId.toString(), actorName, text, at, link);
        afterCommit(() -> send("activity-out-0", event));
    }

    private void send(String binding, Object event) {
        try {
            if (!bridge.send(binding, event)) {
                log.warn("{} was not accepted by the binder: {}", binding, event);
            }
        } catch (RuntimeException e) {
            log.warn("Could not publish to {}: {}", binding, e.getMessage());
        }
    }

    private static void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }

    public record Link(String kind, String teamId, Assessment assessment) {

        public static Link team(UUID teamId, Assessment assessment) {
            return new Link("TEAM", teamId.toString(), assessment);
        }

        public static Link of(String kind, UUID teamId, Assessment assessment) {
            return new Link(kind, teamId == null ? null : teamId.toString(), assessment);
        }
    }

    public record GradeSubmitted(String demoSession, String teamId, Assessment assessment) {
    }

    public record NotificationEvent(String eventKey, String demoSession, String type, String text, Instant at,
                                    Link link, List<String> recipients) {
    }

    public record ActivityEvent(String eventId, String demoSession, String kind, String actorId, String actorName,
                                String text, Instant at, Link link) {
    }
}
