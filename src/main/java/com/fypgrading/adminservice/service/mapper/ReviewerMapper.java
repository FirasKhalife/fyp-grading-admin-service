package com.fypgrading.adminservice.service.mapper;

import com.fypgrading.adminservice.entity.Reviewer;
import com.fypgrading.adminservice.service.dto.ReviewerDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewerMapper {

    ReviewerDTO toDTO(Reviewer rubric);

    Reviewer toEntity(ReviewerDTO rubricDTO);

    List<ReviewerDTO> toDTOList(List<Reviewer> rubrics);

    List<Reviewer> toEntityList(List<ReviewerDTO> rubricDTOs);
}
