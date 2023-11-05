package com.fypgrading.adminservice.service.dto;

import com.fypgrading.adminservice.entity.ReviewerTeam;
import com.fypgrading.adminservice.entity.Student;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamDTO {

    private Integer id;

    private List<Integer> reviewerTeamsIDs;

    private List<Integer> studentIDs;
}
