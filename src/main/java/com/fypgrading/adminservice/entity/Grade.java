package com.fypgrading.adminservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Grade extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private Assessment assessment;

    private Float grade;

    @ManyToOne
    private ReviewerTeam reviewerTeam;

    public Grade(Assessment assessment, Float grade, ReviewerTeam reviewerTeam) {
        this.reviewerTeam = reviewerTeam;
        this.assessment = assessment;
        this.grade = grade;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Grade g)) return false;
        return id.equals(g.getId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Grade{" +
                "id=" + id +
                ", assessment=" + assessment +
                ", grade=" + grade +
                '}';
    }
}

