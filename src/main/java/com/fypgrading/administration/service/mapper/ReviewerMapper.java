package com.fypgrading.administration.service.mapper;

import com.fypgrading.administration.entity.Reviewer;
import com.fypgrading.administration.service.dto.ReviewerDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface ReviewerMapper {

    ReviewerDTO toDTO(Reviewer rubric);

    Reviewer toEntity(ReviewerDTO rubricDTO);

    List<ReviewerDTO> toDTOList(List<Reviewer> rubrics);

    List<Reviewer> toEntityList(List<ReviewerDTO> rubricDTOs);
}
