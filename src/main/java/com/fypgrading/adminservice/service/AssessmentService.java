package com.fypgrading.adminservice.service;

import com.fypgrading.adminservice.entity.Assessment;
import com.fypgrading.adminservice.repository.AssessmentRepository;
import com.fypgrading.adminservice.service.enums.AssessmentEnum;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AssessmentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssessmentService.class);

    private final AssessmentRepository assessmentRepository;

    public AssessmentService(AssessmentRepository assessmentRepository) {
        this.assessmentRepository = assessmentRepository;
        LOGGER.info("AssessmentService initialized");
    }

    public Assessment getAssessmentById(Integer id) {
        LOGGER.info("Retrieving assessment by ID: {}", id);
        return assessmentRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.error("Assessment not found for ID: {}", id);
                    return new EntityNotFoundException("Assessment not found");
                });
    }

    public Assessment getAssessmentByEnum(AssessmentEnum assessmentEnum) {
        LOGGER.info("Retrieving assessment by enum: {}", assessmentEnum);
        return getAssessmentById(assessmentEnum.getEnumId());
    }

    public Assessment getAssessmentByLowerCaseName(String name) {
        LOGGER.info("Retrieving assessment by lower case name: {}", name);
        return getAssessmentById(AssessmentEnum.valueOf(name.toUpperCase()).getEnumId());
    }

    public Assessment getAssessmentByUpperCaseName(String name) {
        LOGGER.info("Retrieving assessment by upper case name: {}", name);
        return getAssessmentById(AssessmentEnum.valueOf(name).getEnumId());
    }
}
