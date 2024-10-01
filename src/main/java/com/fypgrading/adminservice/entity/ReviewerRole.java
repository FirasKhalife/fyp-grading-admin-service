package com.fypgrading.adminservice.entity;

import com.fypgrading.adminservice.service.enums.ReviewerRoleEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ReviewerRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private ReviewerRoleEnum name;

    @ToString.Exclude
    @OneToMany(mappedBy = "reviewerRole", fetch = FetchType.EAGER)
    private List<Assessment> assessments;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ReviewerRole role)) return false;
        return id.equals(role.getId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
