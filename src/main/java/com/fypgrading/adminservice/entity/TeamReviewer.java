package com.fypgrading.adminservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Entity
public class TeamReviewer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Reviewer reviewer;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Team team;

    @ManyToMany
    @JoinTable(name = "team_reviewer_role")
    private List<ReviewerRole> reviewerRoles;

    @OneToMany(mappedBy = "teamReviewer")
    private List<Grade> grades;

    public TeamReviewer(Reviewer reviewer, Team team) {
        this.reviewer = reviewer;
        this.team = team;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TeamReviewer teamReviewer)) return false;
        return this.reviewer.equals(teamReviewer.getReviewer())
                && this.team.equals(teamReviewer.getTeam());
    }

    @Override
    public int hashCode() {
        return reviewer.hashCode() + team.hashCode();
    }

}
