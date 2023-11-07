package com.fypgrading.adminservice.service.mapper;

import com.fypgrading.adminservice.service.dto.RoleDTO;
import com.fypgrading.adminservice.service.enums.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface RoleMapper {

    @Mapping(source = "role", target = "role")
    RoleDTO toDTO(Role role);

    @Mapping(source = "role", target = "role")
    Role toEntity(RoleDTO roleDTO);
}
