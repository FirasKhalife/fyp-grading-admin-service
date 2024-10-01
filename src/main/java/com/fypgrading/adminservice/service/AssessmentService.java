package com.fypgrading.adminservice.service;

import com.fypgrading.adminservice.entity.Assessment;
import com.fypgrading.adminservice.repository.AssessmentRepository;
import com.fypgrading.adminservice.service.enums.AssessmentEnum;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;

    public Assessment getAssessmentById(Long id) {
        return assessmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assessment not found"));
    }

    public Assessment getAssessmentByLowerCaseName(String name) {
        return getAssessmentById(AssessmentEnum.valueOf(name.toUpperCase()).getInstanceId());
    }

    public Assessment getAssessmentByUpperCaseName(String name) {
        return getAssessmentById(AssessmentEnum.valueOf(name).getInstanceId());
    }
}
