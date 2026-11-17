package com.fypgrading.adminservice.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamAssessmentRepository extends JpaRepository<TeamAssessment, UUID> {

    List<TeamAssessment> findByTeamId(UUID teamId);

    Optional<TeamAssessment> findByTeamIdAndAssessment(UUID teamId, Assessment assessment);

    List<TeamAssessment> findByAssessment(Assessment assessment);

    /** Locks the rows, so two release clicks run one after the other. */
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select t from TeamAssessment t where t.assessment = :assessment")
    List<TeamAssessment> lockByAssessment(
            @org.springframework.data.repository.query.Param("assessment") Assessment assessment);

    void deleteByTeamId(UUID teamId);
}
