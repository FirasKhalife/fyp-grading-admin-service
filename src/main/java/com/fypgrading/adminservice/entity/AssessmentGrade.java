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
public class AssessmentGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private AssessmentEnum assessment;

    private Float grade;

    @ManyToOne(fetch = FetchType.EAGER)
    private com.fypgrading.adminservice.entity.ReviewerTeam reviewerTeam;

    public AssessmentGrade(AssessmentEnum assessment, Float grade, com.fypgrading.adminservice.entity.ReviewerTeam reviewerTeam) {
        this.assessment = assessment;
        this.grade = grade;
        this.reviewerTeam = reviewerTeam;
    }
}

