package com.fypgrading.adminservice.repository;

import com.fypgrading.adminservice.entity.Grade;
import com.fypgrading.adminservice.service.enums.AssessmentEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Integer> {

    List<Grade> findAllByAssessment(AssessmentEnum assessment);

    Optional<Grade> findByAssessmentAndReviewerTeam_ReviewerIdAndReviewerTeam_TeamId(
            AssessmentEnum assessmentEnum, Integer reviewerId, Integer teamId
    );

    Long countByAssessmentAndReviewerTeam_TeamId(AssessmentEnum assessment, Integer teamId);

    Long countByReviewerTeam_TeamId(Integer teamId);

    List<Grade> findAllByReviewerTeam_ReviewerIdAndReviewerTeam_TeamId(Integer reviewerId, Integer teamId);

    List<Grade> findALlByReviewerTeam_TeamIdAndAssessment(Integer teamId, AssessmentEnum assessment);
}
