package com.fypgrading.adminservice.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeRepository extends JpaRepository<Grade, UUID> {

    List<Grade> findByTeamIdAndAssessment(UUID teamId, Assessment assessment);

    List<Grade> findByTeamId(UUID teamId);

    Optional<Grade> findByEvaluationId(UUID evaluationId);

    Optional<Grade> findByTeamIdAndAssessmentAndReviewerId(UUID teamId, Assessment assessment, UUID reviewerId);

    boolean existsByTeamId(UUID teamId);

    boolean existsByTeamIdAndReviewerId(UUID teamId, UUID reviewerId);
}
