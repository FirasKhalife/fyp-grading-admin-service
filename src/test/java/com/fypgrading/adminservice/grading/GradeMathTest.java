package com.fypgrading.adminservice.grading;

import static org.assertj.core.api.Assertions.assertThat;

import com.fypgrading.adminservice.domain.Assessment;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GradeMathTest {

    @Test
    void teamGradeIsThePlainAverage() {
        assertThat(GradeMath.display(GradeMath.average(List.of(88.75, 85.0, 86.25))))
                .isEqualTo(86.7);
    }

    @Test
    void finalGradeWeighs15_0_25_30_30AndNeedsAllFiveIncludingTheProgressReport() {
        Map<Assessment, Double> alpha = new EnumMap<>(Assessment.class);
        alpha.put(Assessment.PROPOSAL_PRESENTATION, 86.666_666);
        alpha.put(Assessment.ADVISOR_ASSESSMENT, 90.0);
        alpha.put(Assessment.FINAL_REPORT, 87.5);
        alpha.put(Assessment.FINAL_PRESENTATION, 87.5);
        assertThat(GradeMath.finalGrade(alpha)).as("progress report missing").isEmpty();

        alpha.put(Assessment.PROGRESS_REPORT, 10.0);
        assertThat(GradeMath.display(GradeMath.finalGrade(alpha).orElseThrow())).isEqualTo(88.0);
        alpha.put(Assessment.PROGRESS_REPORT, 95.0);
        assertThat(GradeMath.display(GradeMath.finalGrade(alpha).orElseThrow()))
                .as("0% weight")
                .isEqualTo(88.0);
    }

    @Test
    void spreadIsMaxMinusMinAndNeedsTwoScores() {
        assertThat(GradeMath.display(
                        GradeMath.spread(List.of(85.0, 63.75, 73.75)).orElseThrow()))
                .isEqualTo(21.3);
        assertThat(GradeMath.spread(List.of(70.0))).isEmpty();
    }

    @Test
    void reviewersDisagreeWhenMoreThanTheThresholdApart() {
        assertThat(GradeMath.disagree(List.of(85.0, 63.75, 73.75), 15)).isTrue();
        assertThat(GradeMath.disagree(List.of(80.0, 95.0), 15))
                .as("exactly 15 is not a disagreement")
                .isFalse();
        assertThat(GradeMath.disagree(List.of(80.0, 90.0), 15)).isFalse();
        assertThat(GradeMath.disagree(List.of(35.0), 15))
                .as("one reviewer can't disagree")
                .isFalse();
    }
}
