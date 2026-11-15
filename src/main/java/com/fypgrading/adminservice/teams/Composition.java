package com.fypgrading.adminservice.teams;

import com.fypgrading.adminservice.domain.ReviewerRole;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Team composition rules (brief 4.3): 1 to 5 reviewers in total, at least one jury member, at least one
 * advisor (otherwise the advisor assessment could never be graded), one role per reviewer.
 */
public final class Composition {

    public static final int MAX_REVIEWERS = 5;

    private Composition() {
    }

    public record Assignment(UUID reviewerId, ReviewerRole role) {
    }

    public record Problem(String code, String message) {
    }

    public record Result(boolean valid, int reviewerCount, int maxReviewers, int juryCount, int advisorCount,
                         List<Problem> problems) {
    }

    public static Result check(List<Assignment> assignments, Set<UUID> knownReviewers) {
        List<Problem> problems = new ArrayList<>();
        Set<UUID> seen = new HashSet<>();
        int jury = 0;
        int advisors = 0;
        for (Assignment a : assignments) {
            if (!seen.add(a.reviewerId())) {
                problems.add(new Problem("DUPLICATE_REVIEWER", "A reviewer can hold only one role on a team."));
            }
            if (!knownReviewers.contains(a.reviewerId())) {
                problems.add(new Problem("UNKNOWN_REVIEWER", "One of the reviewers doesn't exist."));
            }
            if (a.role() == ReviewerRole.JURY) {
                jury++;
            } else {
                advisors++;
            }
        }
        int count = assignments.size();
        if (count == 0) {
            problems.add(new Problem("NO_REVIEWERS", "Needs at least 1 reviewer."));
        }
        if (count > MAX_REVIEWERS) {
            problems.add(new Problem("TOO_MANY_REVIEWERS", "A team has at most " + MAX_REVIEWERS + " reviewers."));
        }
        if (jury == 0) {
            problems.add(new Problem("NO_JURY", "Needs at least 1 jury member."));
        }
        if (advisors == 0) {
            problems.add(new Problem("NO_ADVISOR", "Needs at least 1 advisor."));
        }
        return new Result(problems.isEmpty(), count, MAX_REVIEWERS, jury, advisors, List.copyOf(problems));
    }
}
