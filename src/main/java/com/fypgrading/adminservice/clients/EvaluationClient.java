package com.fypgrading.adminservice.clients;

import com.fypgrading.adminservice.domain.Assessment;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** evaluation-service owns drafts and per-criterion levels and comments. */
@Component
public class EvaluationClient {

    private static final Logger log = LoggerFactory.getLogger(EvaluationClient.class);

    private final RestClient http;

    public EvaluationClient(
            @Qualifier(ClientConfig.LOAD_BALANCED) RestClient.Builder loadBalanced,
            @Value("${assay.clients.evaluation:http://evaluation-service}") String base) {
        this.http = loadBalanced.baseUrl(base).build();
    }

    /**
     * Status of every started evaluation in this demo copy. If evaluation-service is down, the screens still work
     * from the stored grades (drafts just show as not started), so this degrades to an empty list.
     */
    public List<Summary> summaries() {
        try {
            List<Summary> body = http.get()
                    .uri("/internal/evaluations/summary")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return body == null ? List.of() : body;
        } catch (RuntimeException e) {
            log.warn("Evaluation summaries unavailable, showing grades only: {}", e.getMessage());
            return List.of();
        }
    }

    /** Submitted evaluations with levels and comments, for the student's released results. */
    public List<Submitted> submitted(UUID teamId, Assessment assessment) {
        return Remote.call(
                "evaluation service",
                () -> http.get()
                        .uri("/internal/evaluations/submitted?teamId={team}&assessment={a}", teamId, assessment)
                        .retrieve()
                        .body(new ParameterizedTypeReference<List<Submitted>>() {}));
    }

    public record Summary(
            UUID evaluationId,
            UUID teamId,
            Assessment assessment,
            UUID reviewerId,
            String status,
            int scoredCount,
            int criteriaCount,
            Double score,
            Instant updatedAt,
            Instant submittedAt) {}

    public record Submitted(
            UUID id,
            UUID teamId,
            Assessment assessment,
            Person reviewer,
            String overallComment,
            Double score,
            List<Criterion> criteria) {}

    public record Person(UUID id, String firstName, String lastName) {}

    public record Criterion(
            UUID criterionId,
            int position,
            String name,
            int weight,
            Map<String, String> descriptions,
            Integer level,
            String comment) {}
}
