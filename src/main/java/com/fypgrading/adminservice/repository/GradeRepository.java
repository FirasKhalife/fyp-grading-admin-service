package com.fypgrading.adminservice.repository;

import com.fypgrading.adminservice.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Integer> {

    List<Grade> findAllByAssessmentId(Integer assessmentId);

    Optional<Grade> findByReviewerTeam_ReviewerIdAndReviewerTeam_TeamIdAndAssessmentId(
            Integer reviewerId, Integer teamId, Integer assessmentId
    );

    Long countByReviewerTeam_TeamIdAndAssessmentId(Integer teamId, Integer assessmentId);

    List<Grade> findAllByReviewerTeam_ReviewerIdAndReviewerTeam_TeamId(Integer reviewerId, Integer teamId);

    List<Grade> findAllByReviewerTeam_TeamIdAndAssessmentId(Integer teamId, Integer assessmentId);
}
