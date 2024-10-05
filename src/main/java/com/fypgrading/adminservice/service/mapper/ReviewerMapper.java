package com.fypgrading.adminservice.service.mapper;

import com.fypgrading.adminservice.entity.Reviewer;
import com.fypgrading.adminservice.service.dto.ReviewerDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReviewerMapper extends EntityMapper<Reviewer, ReviewerDTO> {
}
