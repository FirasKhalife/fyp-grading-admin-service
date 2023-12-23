package com.fypgrading.adminservice.service;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fypgrading.adminservice.Aspect.LoggingAspect;
import com.fypgrading.adminservice.entity.Assessment;
import com.fypgrading.adminservice.repository.AssessmentRepository;
import com.fypgrading.adminservice.service.enums.AssessmentEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

//@SpringBootTest
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
        Logger logger = (Logger) LoggerFactory.getLogger(LoggingAspect.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        ((ch.qos.logback.classic.Logger) logger).addAppender(listAppender);
    }

    @Test
    void getAssessmentById() {

        Integer id = 1;
        Assessment assessmentEntity = new Assessment(id, AssessmentEnum.PROPOSAL_PRESENTATION, 10, null);
        when(assessmentRepository.findById(id)).thenReturn(Optional.of(assessmentEntity));

        List<Assessment> assessments = assessmentRepository.findAllByRoleInOrderById(List.of());
        assertNotNull(assessments);

        List<ILoggingEvent> logsList = listAppender.list;
        assertFalse(logsList.isEmpty()); // Check that logs are present
        assertTrue(logsList.stream().anyMatch(event -> event.getMessage().contains("method - getAssessmentById"))); // Specific log message check

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