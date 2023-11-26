package com.fypgrading.adminservice.entity;

import com.fypgrading.adminservice.service.enums.RoleEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private RoleEnum name;

    @ToString.Exclude
    @OneToMany(mappedBy = "role", fetch = FetchType.EAGER)
    private List<Assessment> assessments;

    public Role(RoleEnum roleEnum) {
        this.id = roleEnum.getInstanceId();
        this.name = roleEnum;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Role role)) return false;
        return id.equals(role.getId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
