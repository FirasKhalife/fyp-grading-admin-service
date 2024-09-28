package com.fypgrading.adminservice.repository;

import com.fypgrading.adminservice.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {

    List<Grade> findAllByAssessmentId(Long assessmentId);

    Optional<Grade> findByTeamReviewer_ReviewerIdAndTeamReviewer_TeamIdAndAssessmentId(
        UUID reviewerId, Long teamId, Long assessmentId
    );

    Long countByTeamReviewer_TeamIdAndAssessmentId(Long teamId, Long assessmentId);

    List<Grade> findAllByTeamReviewer_ReviewerIdAndTeamReviewer_TeamId(UUID reviewerId, Long teamId);

    List<Grade> findAllByTeamReviewer_TeamIdAndAssessmentId(Long teamId, Long assessmentId);
}
