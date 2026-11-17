package com.fypgrading.adminservice.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, UUID> {

    List<Student> findByTeamIdOrderByLastNameAscFirstNameAsc(UUID teamId);

    List<Student> findAllByOrderByLastNameAsc();

    Optional<Student> findByUserId(UUID userId);

    Optional<Student> findByIdAndTeamId(UUID id, UUID teamId);

    boolean existsByEmailIgnoreCase(String email);

    long countByTeamId(UUID teamId);

    void deleteByTeamId(UUID teamId);
}
