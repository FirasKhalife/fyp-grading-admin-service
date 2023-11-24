package com.fypgrading.adminservice.service.mapper;

import com.fypgrading.adminservice.entity.Role;
import com.fypgrading.adminservice.service.enums.RoleEnum;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    default Role toEntity(RoleEnum role) {
        return new Role(role);
    }

    default RoleEnum toEnum(Role role) {
        return role.getName();
    }

    List<Role> toEntityList(List<RoleEnum> roles);

    List<RoleEnum> toEnumList(List<Role> roles);

}
