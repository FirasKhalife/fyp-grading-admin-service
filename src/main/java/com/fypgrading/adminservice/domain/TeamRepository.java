package com.fypgrading.adminservice.domain;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamRepository extends JpaRepository<Team, UUID> {

    List<Team> findAllByOrderByNameAsc();

    Page<Team> findAllByOrderByNameAsc(Pageable pageable);

    boolean existsByNameIgnoreCase(String name);

    /** Serialises grade aggregation per team (SELECT ... FOR UPDATE), so two last grades can't both finalise. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Team t where t.id = :id")
    Optional<Team> lockById(@Param("id") UUID id);
}
