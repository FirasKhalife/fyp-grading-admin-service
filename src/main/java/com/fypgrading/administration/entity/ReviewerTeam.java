package com.fypgrading.administration.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ReviewerTeam {

    @EmbeddedId
    private ReviewerTeamKey id;

    @ManyToOne
    @MapsId("reviewerId")
    @JoinColumn(name = "reviewer_id")
    private Reviewer jury;

    @ManyToOne
    @MapsId("teamId")
    @JoinColumn(name = "team_id")
    private Team team;

    private Integer grade;
}
