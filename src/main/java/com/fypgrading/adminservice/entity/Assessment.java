package com.fypgrading.adminservice.entity;

import com.fypgrading.adminservice.service.enums.AssessmentEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Assessment {

    @Id
    private Integer id;

    @Enumerated(EnumType.STRING)
    private AssessmentEnum name;

    private Integer weight;

    @ManyToOne
    private Role role;

    public Assessment(AssessmentEnum assessmentEnum) {
        this.id = assessmentEnum.getEnumId();
        this.name = assessmentEnum;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Assessment assessment)) return false;
        return this.getId().equals(assessment.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
