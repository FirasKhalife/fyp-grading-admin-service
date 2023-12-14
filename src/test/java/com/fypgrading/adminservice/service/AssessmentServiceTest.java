package com.fypgrading.adminservice.service;

import com.fypgrading.adminservice.entity.Assessment;
import com.fypgrading.adminservice.repository.AssessmentRepository;
import com.fypgrading.adminservice.service.enums.AssessmentEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

//@SpringBootTest
class AssessmentServiceTest {

//    @Autowired
    private AssessmentService assessmentService;

    @Mock
    private AssessmentRepository assessmentRepository;

    @BeforeEach
    void setUp() {
        assessmentService = new AssessmentService(assessmentRepository);
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAssessmentById() {

        Integer id = 1;
        Assessment assessmentEntity = new Assessment(id, AssessmentEnum.PROPOSAL_PRESENTATION, 10, null);
        when(assessmentRepository.findById(id)).thenReturn(Optional.of(assessmentEntity));

        List<Assessment> assessments = assessmentRepository.findAllByRoleInOrderById(List.of());
        assertNotNull(assessments);

    }


    @Test
    void getAssessmentByEnum() {
    }

    @Test
    void getAssessmentByLowerCaseName() {
    }

    @Test
    void getAssessmentByUpperCaseName() {
    }
}