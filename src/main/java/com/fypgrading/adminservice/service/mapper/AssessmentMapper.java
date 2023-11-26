package com.fypgrading.adminservice.service.mapper;

import com.fypgrading.adminservice.entity.Assessment;
import com.fypgrading.adminservice.service.dto.AssessmentDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {RoleMapper.class}
)
public interface AssessmentMapper {

    AssessmentDTO toDTO(Assessment assessment);

    Assessment toEntity(AssessmentDTO assessmentDTO);

    List<AssessmentDTO> toDTOList(List<Assessment> assessments);

}
