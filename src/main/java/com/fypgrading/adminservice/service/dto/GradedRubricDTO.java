package com.fypgrading.adminservice.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradedRubricDTO {

    private String name;

    private Integer percentage;

    private Integer grade;
}
