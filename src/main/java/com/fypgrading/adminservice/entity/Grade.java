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
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private AssessmentEnum assessment;

    private Float grade;

    @ManyToOne
    private ReviewerTeam reviewerTeam;

    public Grade(AssessmentEnum assessment, Float grade, com.fypgrading.adminservice.entity.ReviewerTeam reviewerTeam) {
        this.reviewerTeam = reviewerTeam;
        this.assessment = assessment;
        this.grade = grade;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Grade g)) return false;
        return id.equals(g.getId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}

