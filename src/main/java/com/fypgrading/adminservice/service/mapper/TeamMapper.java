package com.fypgrading.adminservice.service.mapper;

import com.fypgrading.adminservice.entity.Team;
import com.fypgrading.adminservice.service.dto.TeamDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TeamMapper extends EntityMapper<Team, TeamDTO> {
}
