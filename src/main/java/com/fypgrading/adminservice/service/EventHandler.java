package com.fypgrading.adminservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fypgrading.adminservice.entity.Assessment;
import com.fypgrading.adminservice.entity.Team;
import com.fypgrading.adminservice.entity.TeamAssessment;
import com.fypgrading.adminservice.repository.GradeRepository;
import com.fypgrading.adminservice.repository.TeamAssessmentRepository;
import com.fypgrading.adminservice.service.dto.TeamDTO;
import com.fypgrading.adminservice.service.enums.AssessmentEnum;
import com.fypgrading.adminservice.service.event.EvaluationSubmittedEvent;
import com.fypgrading.adminservice.service.event.GradeFinalizedEvent;
import com.fypgrading.adminservice.service.mapper.AssessmentMapper;
import com.fypgrading.adminservice.service.mapper.TeamMapper;
import com.rabbitmq.client.Channel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventHandler implements ChannelAwareMessageListener {

    private final Logger logger = LoggerFactory.getLogger(EventHandler.class);
    private final TeamAssessmentRepository teamAssessmentRepository;
    private final AssessmentMapper assessmentMapper;
    private final EventDispatcher eventDispatcher;
    private final GradeRepository gradeRepository;
    private final ReviewerService reviewerService;
    private final RabbitService rabbitService;
    @PersistenceContext
    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;
    private final TeamService teamService;
    private final TeamMapper teamMapper;

    public EventHandler(TeamAssessmentRepository teamAssessmentRepository,
                        AssessmentMapper assessmentMapper,
                        EventDispatcher eventDispatcher,
                        GradeRepository gradeRepository,
                        ReviewerService reviewerService,
                        RabbitService rabbitService,
                        EntityManager entityManager,
                        ObjectMapper objectMapper,
                        TeamService teamService,
                        TeamMapper teamMapper
    ) {
        this.teamAssessmentRepository = teamAssessmentRepository;
        this.assessmentMapper = assessmentMapper;
        this.eventDispatcher = eventDispatcher;
        this.gradeRepository = gradeRepository;
        this.reviewerService = reviewerService;
        this.rabbitService = rabbitService;
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
        this.teamService = teamService;
        this.teamMapper = teamMapper;
    }

    @Override
    @Transactional
    public void onMessage(Message message, Channel channel) {
        try {
            logger.info("Received message: " + message);

            EvaluationSubmittedEvent event =
                    objectMapper.readValue(
                            new String(message.getBody()),
                            EvaluationSubmittedEvent.class
                    );
            TeamDTO teamDTO = event.getTeam();

            //number of reviewers for this team assessment
            long teamReviewersCount =
                    reviewerService.countReviewersByTeamIdAndAssessmentId(
                            teamDTO.getId(), event.getAssessment().getId()
                    );

            // number of submitted reviews for this team assessment
            long submittedAssessmentTeamReviewsCount =
                    gradeRepository.countByReviewerTeam_TeamIdAndAssessmentId(
                            teamDTO.getId(), event.getAssessment().getId()
                    );

            if (teamReviewersCount != submittedAssessmentTeamReviewsCount)
                return;

            // if all reviewers submitted their reviews for this team assessment,
            // calculate final grade for this assessment, save it, and send notification
            float teamAssessmentGrade = (float) (double)
                            gradeRepository.findAllByReviewerTeam_TeamIdAndAssessmentId(
                                    teamDTO.getId(), event.getAssessment().getId()
                            ).stream().reduce(0.0, (acc, gradeUnit) ->
                                    acc + (double) gradeUnit.getGrade(), Double::sum) / teamReviewersCount;

            Team team = entityManager.merge(teamMapper.toEntity(teamDTO));
            Assessment assessment = assessmentMapper.toEntity(event.getAssessment());
            teamAssessmentRepository.save(new TeamAssessment(team, assessment, teamAssessmentGrade));

            GradeFinalizedEvent assessmentGradeFinalizedEvent =
                    new GradeFinalizedEvent(teamDTO.getId(), event.getAssessment().getName(), LocalDateTime.now());
            eventDispatcher.sendAdminNotification(assessmentGradeFinalizedEvent);

            List<TeamAssessment> gradedAssessments = teamAssessmentRepository.findAllByTeamId(teamDTO.getId());

            if (gradedAssessments.size() != AssessmentEnum.values().length) {
                return;
            }

            // if all assessments are finally graded,
            // calculate final grade for this team, save it, and send notification
            float finalGrade =
                    (float) (double) gradedAssessments
                            .stream()
                            .reduce(0.0, (acc, gradedAssessment) ->
                                    acc + (double) gradedAssessment.getGrade()
                                            * gradedAssessment.getAssessment().getWeight() / 100, Double::sum);

            teamService.saveFinalGrade(team, finalGrade);

            eventDispatcher.sendAdminNotification(
                    new GradeFinalizedEvent(teamDTO.getId(), null, LocalDateTime.now())
            );

        } catch (Exception ex) {
            logger.warn("Exception: " + ex.getMessage());
            rabbitService.sendNack(message, channel);
        }
    }

}

