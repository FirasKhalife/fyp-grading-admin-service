package com.fypgrading.adminservice.domain;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DemoCopyRepository extends JpaRepository<DemoCopy, UUID> {

    boolean existsByCreatedAtBefore(Instant instant);
}
