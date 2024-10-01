package com.fypgrading.adminservice.service.mapper;

import com.fypgrading.adminservice.entity.ReviewerRole;
import com.fypgrading.adminservice.service.enums.ReviewerRoleEnum;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    default ReviewerRoleEnum toEnum(ReviewerRole role) {
        return role.getName();
    }

    List<ReviewerRoleEnum> toEnumList(List<ReviewerRole> roles);

}
