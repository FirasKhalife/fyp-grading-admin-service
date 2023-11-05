package com.fypgrading.administration.service.mapper;

import com.fypgrading.administration.service.dto.RubricDTO;
import com.fypgrading.administration.entity.Rubric;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface RubricMapper {

    RubricDTO toDTO(Rubric rubric);

    Rubric toEntity(RubricDTO rubricDTO);

    List<RubricDTO> toDTOList(List<Rubric> rubrics);

    List<Rubric> toEntityList(List<RubricDTO> rubricDTOs);
}
