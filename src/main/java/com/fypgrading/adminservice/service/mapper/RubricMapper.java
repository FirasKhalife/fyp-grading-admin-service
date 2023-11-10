package com.fypgrading.adminservice.service.mapper;

import com.fypgrading.adminservice.service.dto.RubricDTO;
import com.fypgrading.adminservice.entity.Rubric;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RubricMapper {

    RubricDTO toDTO(Rubric rubric);

    Rubric toEntity(RubricDTO rubricDTO);

    List<RubricDTO> toDTOList(List<Rubric> rubrics);

    List<Rubric> toEntityList(List<RubricDTO> rubricDTOs);
}
