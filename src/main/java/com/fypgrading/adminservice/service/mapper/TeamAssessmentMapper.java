package com.fypgrading.adminservice.service.mapper;

import com.fypgrading.adminservice.entity.TeamAssessment;
import com.fypgrading.adminservice.service.dto.TeamAssessmentDTO;
import org.mapstruct.Mapper;

@Mapper(
    componentModel = "spring",
    uses = {
        TeamMapper.class,
        AssessmentMapper.class
    }
)
public interface TeamAssessmentMapper {

    TeamAssessmentDTO toDTO(TeamAssessment assessment);

}
