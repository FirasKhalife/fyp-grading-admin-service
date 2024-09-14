package com.fypgrading.adminservice.service.mapper;

import com.fypgrading.adminservice.entity.Grade;
import com.fypgrading.adminservice.service.dto.GradeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(
    componentModel = "spring",
    uses = {
        TeamMapper.class,
        ReviewerMapper.class,
        AssessmentMapper.class
    }
)
public interface GradeMapper {

    @Mapping(source = "teamReviewer.reviewer", target = "reviewer")
    @Mapping(source = "teamReviewer.team", target = "team")
    GradeDTO toTeamGradeDTO(Grade grade);

    List<GradeDTO> toTeamGradeDTOList(List<Grade> gradeList);


}
