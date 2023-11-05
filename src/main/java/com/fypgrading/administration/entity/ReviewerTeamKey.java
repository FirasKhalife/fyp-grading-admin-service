package com.fypgrading.administration.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ReviewerTeamKey implements Serializable {

    @Column(name = "reviewer_id")
    private Integer reviewerId;

    @Column(name = "team_id")
    private Integer teamId;
}
