package com.fypgrading.adminservice.domain;

import com.fypgrading.adminservice.common.jpa.AssignedIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.TenantId;

/** A team member. {@code userId} links the row to a Keycloak account (only the student persona has one). */
@Entity
@Table(name = "student")
public class Student extends AssignedIdEntity {

    @TenantId
    @Column(name = "demo_session", nullable = false, updatable = false)
    private String demoSession;

    @Column(name = "team_id", nullable = false)
    private UUID teamId;

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Column(nullable = false, length = 200)
    private String email;

    @Column(name = "user_id")
    private UUID userId;

    protected Student() {
    }

    public Student(UUID id, UUID teamId, String firstName, String lastName, String email, UUID userId) {
        super(id);
        this.teamId = teamId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.userId = userId;
    }

    public void update(String firstName, String lastName, String email, UUID teamId) {
        if (firstName != null) {
            this.firstName = firstName;
        }
        if (lastName != null) {
            this.lastName = lastName;
        }
        if (email != null) {
            this.email = email;
        }
        if (teamId != null) {
            this.teamId = teamId;
        }
    }

    public UUID getTeamId() {
        return teamId;
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

    public UUID getUserId() {
        return userId;
    }
}
