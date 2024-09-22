package com.fypgrading.adminservice.service;

import com.fypgrading.adminservice.entity.Assessment;
import com.fypgrading.adminservice.repository.AssessmentRepository;
import com.fypgrading.adminservice.service.enums.AssessmentEnum;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class AssessmentService {
    
    private static final Logger logger = LoggerFactory.getLogger(AssessmentService.class);

    private final AssessmentRepository assessmentRepository;

    public Assessment getAssessmentById(Long id) {
        logger.debug("Retrieving assessment by ID: {}", id);
        return assessmentRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Assessment not found for ID: {}", id);
                    return new EntityNotFoundException("Assessment not found");
                });
    }

    public Assessment getAssessmentByEnum(AssessmentEnum assessmentEnum) {
        return getAssessmentById(assessmentEnum.getInstanceId());
    }

    public Assessment getAssessmentByLowerCaseName(String name) {
        return getAssessmentById(AssessmentEnum.valueOf(name.toUpperCase()).getInstanceId());
    }

    public Assessment getAssessmentByUpperCaseName(String name) {
        return getAssessmentById(AssessmentEnum.valueOf(name).getInstanceId());
    }
}
