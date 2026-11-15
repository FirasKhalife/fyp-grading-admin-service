package com.fypgrading.adminservice.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DefenseSlotRepository extends JpaRepository<DefenseSlot, UUID> {

    Optional<DefenseSlot> findByTeamId(UUID teamId);

    List<DefenseSlot> findAllByOrderByStartsAtAsc();

    void deleteByTeamId(UUID teamId);
}
