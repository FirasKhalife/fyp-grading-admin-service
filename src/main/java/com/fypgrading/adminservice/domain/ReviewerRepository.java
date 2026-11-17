package com.fypgrading.adminservice.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewerRepository extends JpaRepository<Reviewer, UUID> {

    List<Reviewer> findAllByOrderByLastNameAscFirstNameAsc();

    List<Reviewer> findByCoordinatorTrue();
}
