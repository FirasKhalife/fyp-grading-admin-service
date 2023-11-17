package com.fypgrading.adminservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Entity(name = "reviewer_team")
public class ReviewerTeam {

    @EmbeddedId
    private ReviewerTeamKey id;

    @ManyToOne
    @MapsId("reviewerId")
    @JoinColumn(name = "reviewer_id")
    private Reviewer reviewer;

    @ManyToOne
    @MapsId("teamId")
    @JoinColumn(name = "team_id")
    private Team team;

    @OneToMany(mappedBy = "reviewerTeam")
    private List<AssessmentGrade> assessmentGrades;

    public ReviewerTeam(Integer reviewerId, Integer teamId) {
        this.id.setReviewerId(reviewerId);
        this.id.setTeamId(teamId);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ReviewerTeam reviewerTeam)) return false;
        return this.id.getReviewerId().equals(reviewerTeam.getId().getReviewerId())
                && this.id.getTeamId().equals(reviewerTeam.getId().getTeamId());
    }

}
