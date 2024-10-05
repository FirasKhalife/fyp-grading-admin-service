package com.fypgrading.adminservice.service.mapper;

import com.fypgrading.adminservice.entity.Assessment;
import com.fypgrading.adminservice.service.dto.AssessmentDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AssessmentMapper extends EntityMapper<Assessment, AssessmentDTO> {
}
