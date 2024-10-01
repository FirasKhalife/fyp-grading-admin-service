package com.fypgrading.adminservice.service.mapper;

import com.fypgrading.adminservice.entity.Reviewer;
import com.fypgrading.adminservice.service.dto.ReviewerDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(
    componentModel = "spring",
    uses = { RoleMapper.class }
)
public interface ReviewerMapper {

    List<ReviewerDTO> toDTOList(List<Reviewer> reviewers);

    ReviewerDTO toDTO(Reviewer reviewer);

}
