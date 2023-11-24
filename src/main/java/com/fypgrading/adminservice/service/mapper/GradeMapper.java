package com.fypgrading.adminservice.service.mapper;

import com.fypgrading.adminservice.entity.Grade;
import com.fypgrading.adminservice.service.dto.TeamGradeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {
                TeamMapper.class,
                ReviewerMapper.class
        }
)
public interface GradeMapper {

    @Mapping(source = "reviewerTeam.reviewer.id", target = "reviewerId")
    @Mapping(source = "reviewerTeam.team.id", target = "teamId")
    TeamGradeDTO toTeamGradeDTO(Grade grade);

    List<TeamGradeDTO> toTeamGradeDTOList(List<Grade> gradeList);


}
