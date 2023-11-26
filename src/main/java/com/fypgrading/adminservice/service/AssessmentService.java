package com.fypgrading.adminservice.service;

import com.fypgrading.adminservice.entity.Assessment;
import com.fypgrading.adminservice.repository.AssessmentRepository;
import com.fypgrading.adminservice.service.enums.AssessmentEnum;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;

    public AssessmentService(AssessmentRepository assessmentRepository) {
        this.assessmentRepository = assessmentRepository;
    }

    public Assessment getAssessmentById(Integer id) {
        return assessmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assessment not found"));
    }

    public Assessment getAssessmentByEnum(AssessmentEnum assessmentEnum) {
        return getAssessmentById(assessmentEnum.getEnumId());
    }

    public Assessment getAssessmentByLowerCaseName(String name) {
        return getAssessmentById(AssessmentEnum.valueOf(name.toUpperCase()).getEnumId());
    }

    public Assessment getAssessmentByUpperCaseName(String name) {
        return getAssessmentById(AssessmentEnum.valueOf(name).getEnumId());
    }
}
