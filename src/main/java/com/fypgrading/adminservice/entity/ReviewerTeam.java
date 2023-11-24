package com.fypgrading.adminservice.entity;

import com.fypgrading.adminservice.entity.idClass.ReviewerTeamId;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Entity
@IdClass(ReviewerTeamId.class)
public class ReviewerTeam {

    @Id
    @ManyToOne
    private Reviewer reviewer;

    @Id
    @ManyToOne
    private Team team;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "team_reviewer_role",
            joinColumns = {
                    @JoinColumn(name = "reviewer_id"),
                    @JoinColumn(name = "team_id")
            },
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> reviewerRoles;

    @OneToMany(mappedBy = "reviewerTeam")
    private List<Grade> grades;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ReviewerTeam reviewerTeam)) return false;
        return this.getReviewer().equals(reviewerTeam.getReviewer())
                && this.getTeam().equals(reviewerTeam.getTeam());
    }

    @Override
    public int hashCode() {
        return getReviewer().hashCode() + getTeam().hashCode();
    }

}
