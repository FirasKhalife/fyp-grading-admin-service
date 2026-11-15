package com.fypgrading.adminservice.clients;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Asks the other services to create or delete their part of a visitor's demo copy. */
@Component
public class DemoCopyClient {

    private final List<Target> targets;

    public DemoCopyClient(RestClient.Builder loadBalanced,
            @Value("${assay.clients.rubric:http://rubric-service}") String rubric,
            @Value("${assay.clients.evaluation:http://evaluation-service}") String evaluation,
            @Value("${assay.clients.notification:http://notification-service}") String notification) {
        this.targets = List.of(
                new Target("rubric service", loadBalanced.clone().baseUrl(rubric).build()),
                new Target("evaluation service", loadBalanced.clone().baseUrl(evaluation).build()),
                new Target("notification service", loadBalanced.clone().baseUrl(notification).build()));
    }

    public void replaceEverywhere(UUID session) {
        for (Target t : targets) {
            Remote.call(t.name(), () -> t.http().put().uri("/internal/demo-sessions/{s}", session).retrieve()
                    .toBodilessEntity());
        }
    }

    /** Best effort: used to roll back a half-created copy; the midnight purge catches anything left. */
    public void deleteEverywhere(UUID session) {
        for (Target t : targets) {
            try {
                t.http().delete().uri("/internal/demo-sessions/{s}", session).retrieve().toBodilessEntity();
            } catch (RuntimeException ignored) {
                // nothing else to do
            }
        }
    }

    private record Target(String name, RestClient http) {
    }
}
