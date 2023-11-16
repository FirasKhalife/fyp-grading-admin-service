package com.fypgrading.adminservice.repository;

import com.fypgrading.adminservice.entity.Rubric;
import com.fypgrading.adminservice.service.enums.AssessmentEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RubricRepository extends JpaRepository<Rubric, Integer> {
    List<Rubric> findByAssessment(AssessmentEnum assessment);

    void deleteAllByAssessment(AssessmentEnum assessment);
}
