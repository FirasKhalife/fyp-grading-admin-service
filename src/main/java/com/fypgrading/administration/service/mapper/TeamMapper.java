package com.fypgrading.administration.service.mapper;

import com.fypgrading.administration.entity.Team;
import com.fypgrading.administration.service.dto.TeamDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface TeamMapper {

    TeamDTO toDTO(Team team);

    Team toEntity(TeamDTO teamDTO);

    List<TeamDTO> toDTOList(List<Team> teams);

    List<Team> toEntityList(List<TeamDTO> teamDTOs);
}
