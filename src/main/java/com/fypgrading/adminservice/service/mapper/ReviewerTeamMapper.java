package com.fypgrading.adminservice.service.mapper;

import com.fypgrading.adminservice.entity.AssessmentGrade;
import com.fypgrading.adminservice.entity.ReviewerTeam;
import com.fypgrading.adminservice.service.dto.ReviewerTeamGradeDTO;
import com.fypgrading.adminservice.service.dto.ReviewerViewDTO;
import com.fypgrading.adminservice.service.dto.TeamDTO;
import com.fypgrading.adminservice.service.dto.TeamGradeDTO;
import org.mapstruct.Mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {
                TeamMapper.class,
                ReviewerMapper.class
        }
)
public interface ReviewerTeamMapper {

    default List<TeamGradeDTO> toTeamGradeDTO(List<AssessmentGrade> assessmentGradeList) {
        List<TeamGradeDTO> teamGrades = new ArrayList<>();
        for (AssessmentGrade assessmentGrade : assessmentGradeList) {
            ReviewerTeam reviewerTeam = assessmentGrade.getReviewerTeam();
            ReviewerTeamGradeDTO reviewerTeamGradeDTO =
                    new ReviewerTeamGradeDTO(
                            new ReviewerViewDTO(reviewerTeam.getReviewer()),
                            assessmentGrade.getGrade()
                    );
            TeamGradeDTO teamGrade =
                    new TeamGradeDTO(
                            assessmentGrade.getAssessment(),
                            new TeamDTO(reviewerTeam.getTeam()),
                            Collections.singletonList(reviewerTeamGradeDTO)
                    );

            int index;
            if ((index = teamGrades.indexOf(teamGrade)) == -1) {
                teamGrades.add(teamGrade);
                continue;
            }
            teamGrades.get(index).addReviewerGrade(reviewerTeamGradeDTO);
        }

        return teamGrades;
    }
}
