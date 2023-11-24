package com.fypgrading.adminservice.entity.idClass;

import com.fypgrading.adminservice.entity.Reviewer;
import com.fypgrading.adminservice.entity.Team;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewerTeamId implements Serializable {

    private Reviewer reviewer;

    private Team team;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ReviewerTeamId reviewerTeamId)) return false;
        return this.getReviewer().equals(reviewerTeamId.getReviewer())
                && getTeam().equals(reviewerTeamId.getTeam());
    }

    @Override
    public int hashCode() {
        return this.getReviewer().hashCode() + this.getTeam().hashCode();
    }
}
