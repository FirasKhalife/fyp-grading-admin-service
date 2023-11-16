package com.fypgrading.adminservice.repository;

import com.fypgrading.adminservice.entity.AssessmentGrade;
import com.fypgrading.adminservice.service.enums.AssessmentEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentGradeRepository extends JpaRepository<AssessmentGrade, Integer> {

    List<AssessmentGrade> findAllByAssessment(AssessmentEnum assessment);

    Optional<AssessmentGrade> findByAssessmentAndReviewerTeam_ReviewerIdAndReviewerTeam_TeamId(
            AssessmentEnum assessmentEnum, Integer reviewerId, Integer teamId
    );

    Long countByAssessmentAndReviewerTeam_TeamId(AssessmentEnum assessment, Integer teamId);

    Long countByReviewerTeam_TeamId(Integer teamId);

}
