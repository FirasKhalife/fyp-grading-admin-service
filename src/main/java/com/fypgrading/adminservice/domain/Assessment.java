package com.fypgrading.adminservice.domain;

import java.util.Arrays;
import java.util.List;

/**
 * The five fixed checkpoints: weight in the final grade (15/0/25/30/30) and the reviewer role that grades it.
 * The progress report weighs 0% but is required before the final grade can be computed.
 */
public enum Assessment {
    PROPOSAL_PRESENTATION("Proposal presentation", "Proposal", 15, ReviewerRole.JURY),
    PROGRESS_REPORT("Progress report", "Progress", 0, ReviewerRole.JURY),
    ADVISOR_ASSESSMENT("Advisor assessment", "Advisor", 25, ReviewerRole.ADVISOR),
    FINAL_REPORT("Final report", "Final report", 30, ReviewerRole.JURY),
    FINAL_PRESENTATION("Final presentation", "Final pres.", 30, ReviewerRole.JURY);

    private final String displayName;
    private final String shortName;
    private final int weight;
    private final ReviewerRole role;

    Assessment(String displayName, String shortName, int weight, ReviewerRole role) {
        this.displayName = displayName;
        this.shortName = shortName;
        this.weight = weight;
        this.role = role;
    }

    public String displayName() {
        return displayName;
    }

    public String shortName() {
        return shortName;
    }

    public int weight() {
        return weight;
    }

    public ReviewerRole role() {
        return role;
    }

    public int position() {
        return ordinal() + 1;
    }

    public static List<Assessment> forRole(ReviewerRole role) {
        return Arrays.stream(values()).filter(a -> a.role == role).toList();
    }
}
