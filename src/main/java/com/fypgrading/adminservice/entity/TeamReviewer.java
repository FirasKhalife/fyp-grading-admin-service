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
    private Reviewer reviewer;

    @ManyToOne
    private Team team;

    @ManyToMany
    @JoinTable(
            name = "team_reviewer_role",
            joinColumns = @JoinColumn(name = "team_reviewer_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> reviewerRoles;

    @OneToMany(mappedBy = "teamReviewer")
    private List<Grade> grades;

    public TeamReviewer(Reviewer reviewer, Team team) {
        this.reviewer = reviewer;
        this.team = team;
    }

    public void addRole(Role role) {
        reviewerRoles.add(role);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TeamReviewer teamReviewer)) return false;
        return this.getReviewer().equals(teamReviewer.getReviewer())
                && this.getTeam().equals(teamReviewer.getTeam());
    }

    @Override
    public int hashCode() {
        return getReviewer().hashCode() + getTeam().hashCode();
    }

}
