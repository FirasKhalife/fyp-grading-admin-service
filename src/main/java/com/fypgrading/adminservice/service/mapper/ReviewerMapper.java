package com.fypgrading.adminservice.service.mapper;

import com.fypgrading.adminservice.entity.Reviewer;
import com.fypgrading.adminservice.service.dto.ReviewerDTO;
import com.fypgrading.adminservice.service.dto.ReviewerViewDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewerMapper {

    List<ReviewerViewDTO> toViewDTOList(List<Reviewer> reviewers);

    List<Reviewer> toEntityList(List<ReviewerDTO> reviewerDTOs);

    ReviewerViewDTO toViewDTO(Reviewer reviewer);

    Reviewer toEntity(ReviewerDTO reviewerDTO);

}
