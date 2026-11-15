package com.fypgrading.adminservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * A faculty member who can be assigned to teams. Global, not part of a demo copy: the id is the Keycloak user
 * id, fixed in the realm export and the seed, so a signed-in reviewer always matches their row.
 */
@Entity
@Table(name = "reviewer")
public class Reviewer {

    @Id
    private UUID id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    /** Also holds the ADMIN role (the coordinator). */
    @Column(nullable = false)
    private boolean coordinator;

    protected Reviewer() {
    }

    public Reviewer(UUID id, String firstName, String lastName, String email, boolean coordinator) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.coordinator = coordinator;
    }

    public UUID getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public boolean isCoordinator() {
        return coordinator;
    }

    public String displayName() {
        return firstName + " " + lastName;
    }
}
