package com.fypgrading.adminservice.service.dto;

import com.fypgrading.adminservice.entity.Team;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamDTO {

    private Long id;

    private Float finalGrade;

    public TeamDTO(Team team) {
        this.id = team.getId();
        this.finalGrade = team.getFinalGrade();
    }

}
