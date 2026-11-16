package com.fypgrading.adminservice.common.jpa;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Transient;
import java.util.Objects;
import java.util.UUID;
import org.springframework.data.domain.Persistable;

/**
 * Entities whose UUID is assigned by the application (random, or derived from the seed key), so
 * {@code save()} inserts without a select first. Equality is by id.
 */
@MappedSuperclass
public abstract class AssignedIdEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @Transient
    private boolean fresh = true;

    protected AssignedIdEntity() {}

    protected AssignedIdEntity(UUID id) {
        this.id = Objects.requireNonNull(id);
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return fresh;
    }

    @PostLoad
    @PostPersist
    void markPersisted() {
        fresh = false;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof AssignedIdEntity e && getClass() == e.getClass() && id.equals(e.id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
