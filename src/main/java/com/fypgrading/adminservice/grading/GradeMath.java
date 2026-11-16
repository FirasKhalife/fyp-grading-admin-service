package com.fypgrading.adminservice.grading;

import com.fypgrading.adminservice.domain.Assessment;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * How scores combine (product brief 4.2):
 * <ul>
 *   <li>Team assessment grade = the plain average of the reviewers' scores, once all of them submitted.</li>
 *   <li>Team final grade = Σ(team assessment grade × weight %), once all five are final. The progress report
 *       weighs 0% but must be final too.</li>
 * </ul>
 * Everything is computed unrounded and shown to one decimal, half up.
 */
public final class GradeMath {

    private GradeMath() {}

    public static double average(Collection<Double> scores) {
        if (scores.isEmpty()) {
            throw new IllegalArgumentException("No scores to average");
        }
        return scores.stream().mapToDouble(Double::doubleValue).sum() / scores.size();
    }

    /** Empty until every assessment has a final grade. */
    public static Optional<Double> finalGrade(Map<Assessment, Double> teamGrades) {
        double total = 0;
        for (Assessment a : Assessment.values()) {
            Double grade = teamGrades.get(a);
            if (grade == null) {
                return Optional.empty();
            }
            total += grade * a.weight() / 100.0;
        }
        return Optional.of(total);
    }

    /** Max minus min of the submitted scores; informational only (the grade is always the plain average). */
    public static Optional<Double> spread(Collection<Double> scores) {
        if (scores.size() < 2) {
            return Optional.empty();
        }
        double max = scores.stream().mapToDouble(Double::doubleValue).max().orElseThrow();
        double min = scores.stream().mapToDouble(Double::doubleValue).min().orElseThrow();
        return Optional.of(max - min);
    }

    /**
     * Plan 3.5: the reviewers disagree when their scores are more than {@code threshold} points apart (15 by
     * default). Informational only: the grade is always the plain average.
     */
    public static boolean disagree(Collection<Double> scores, double threshold) {
        return spread(scores).map(s -> s > threshold).orElse(false);
    }

    public static Double display(Double score) {
        return score == null
                ? null
                : BigDecimal.valueOf(score).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
