package com.fypgrading.adminservice.domain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DemoCopyRepository extends JpaRepository<DemoCopy, UUID> {
}
