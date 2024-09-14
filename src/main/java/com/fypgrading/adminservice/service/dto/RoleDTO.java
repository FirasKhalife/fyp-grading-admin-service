package com.fypgrading.adminservice.service.dto;

import com.fypgrading.adminservice.service.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {

    private Long id;

    private RoleEnum name;

    private List<AssessmentDTO> assessment;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RoleDTO role)) return false;
        return role.getId().equals(this.getId());
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }

}
