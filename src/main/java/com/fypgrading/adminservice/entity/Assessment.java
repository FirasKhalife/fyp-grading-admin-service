package com.fypgrading.adminservice.entity;

import com.fypgrading.adminservice.service.enums.AssessmentEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Assessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private AssessmentEnum name;

    @Column(nullable = false)
    private Integer weight;

    @ManyToOne
    @JoinColumn(nullable = false)
    private ReviewerRole reviewerRole;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Assessment assessment)) return false;
        return Objects.equals(id, assessment.getId());
    }

    @Override
    public int hashCode() {
        return 10 * this.id.hashCode();
    }

    @Override
    public String toString() {
        return "Assessment{" +
            "id=" + id +
            ", name=" + name +
            ", weight=" + weight +
            ", role=" + reviewerRole +
            '}';
    }
}
