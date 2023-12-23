package com.fypgrading.adminservice.service;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fypgrading.adminservice.entity.Assessment;
import com.fypgrading.adminservice.repository.AssessmentRepository;
import com.fypgrading.adminservice.service.enums.AssessmentEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.Optional;

import static org.mockito.Mockito.when;

//@SpringBootTest
@EnableAspectJAutoProxy
class AssessmentServiceTest {

//    @Autowired
    private AssessmentService assessmentService;

    private ListAppender<ILoggingEvent> listAppender;

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

    }


    @Test
    void getAssessmentByEnum() {
        Integer id = 1;
        AssessmentEnum assessmentEnum = AssessmentEnum.PROPOSAL_PRESENTATION;
        Assessment assessmentEntity = new Assessment(id, assessmentEnum, 20, null);
        when(assessmentRepository.findById(id)).thenReturn(Optional.of(assessmentEntity));
    }


    @Test
    void getAssessmentByLowerCaseName() {
        Integer id = 1;
        String lowerCaseName = "proposal_presentation";
        AssessmentEnum assessmentEnum = AssessmentEnum.valueOf(lowerCaseName.toUpperCase());
        Assessment assessmentEntity = new Assessment(id, assessmentEnum, 30, null);
        when(assessmentRepository.findById(id)).thenReturn(Optional.of(assessmentEntity));
    }

    @Test
    void getAssessmentByUpperCaseName() {
        Integer id = 1;
        String upperCaseName = "PROPOSAL_Presentation";
        AssessmentEnum assessmentEnum = AssessmentEnum.valueOf(upperCaseName);
        Assessment assessmentEntity = new Assessment(id, assessmentEnum, 40, null);
        when(assessmentRepository.findById(id)).thenReturn(Optional.of(assessmentEntity));
    }
}