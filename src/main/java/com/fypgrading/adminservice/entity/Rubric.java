package com.fypgrading.adminservice.entity;

import com.fypgrading.adminservice.service.enums.AssessmentEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Rubric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    private String name;

    @NotNull
    private Integer percentage;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AssessmentEnum assessment;
}
