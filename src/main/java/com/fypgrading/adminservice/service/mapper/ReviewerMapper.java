package com.fypgrading.adminservice.service.mapper;

import com.fypgrading.adminservice.entity.Reviewer;
import com.fypgrading.adminservice.service.dto.ReviewerDTO;
import com.fypgrading.adminservice.service.dto.ReviewerLoginDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {RoleMapper.class}
)
public interface ReviewerMapper {

    List<ReviewerDTO> toDTOList(List<Reviewer> reviewers);

    List<Reviewer> toEntityList(List<ReviewerLoginDTO> reviewerLoginDTOS);

    ReviewerDTO toDTO(Reviewer reviewer);

    Reviewer toEntity(ReviewerLoginDTO reviewerLoginDTO);

}
