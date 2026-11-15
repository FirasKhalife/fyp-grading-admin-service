package com.fypgrading.adminservice.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamAssessmentRepository extends JpaRepository<TeamAssessment, UUID> {

    List<TeamAssessment> findByTeamId(UUID teamId);

    Optional<TeamAssessment> findByTeamIdAndAssessment(UUID teamId, Assessment assessment);

    List<TeamAssessment> findByAssessment(Assessment assessment);

    void deleteByTeamId(UUID teamId);
}
