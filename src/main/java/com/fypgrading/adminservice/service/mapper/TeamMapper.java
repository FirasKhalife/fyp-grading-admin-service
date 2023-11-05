package com.fypgrading.adminservice.service.mapper;

import com.fypgrading.adminservice.entity.Team;
import com.fypgrading.adminservice.service.dto.TeamDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface TeamMapper {

    TeamDTO toDTO(Team team);

    Team toEntity(TeamDTO teamDTO);

    List<TeamDTO> toDTOList(List<Team> teams);

    List<Team> toEntityList(List<TeamDTO> teamDTOs);
}
