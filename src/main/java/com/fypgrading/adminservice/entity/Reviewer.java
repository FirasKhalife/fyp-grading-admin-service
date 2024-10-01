package com.fypgrading.adminservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Reviewer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @NotNull
    @Column(nullable = false, unique = true)
    private String email;

    @NotNull
    @Column(nullable = false)
    private String firstName;

    @NotNull
    @Column(nullable = false)
    private String lastName;

    @ManyToMany
    @JoinTable(name = "user_system_role")
    private List<SystemRole> roles;

    @ToString.Exclude
    @OneToMany(mappedBy = "reviewer")
    private List<TeamReviewer> teams;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Reviewer reviewer)) return false;
        return id.equals(reviewer.getId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
