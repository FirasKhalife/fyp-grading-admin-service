package com.fypgrading.adminservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fypgrading.adminservice.entity.Team;
import com.fypgrading.adminservice.entity.TeamAssessmentGrade;
import com.fypgrading.adminservice.repository.GradeRepository;
import com.fypgrading.adminservice.repository.TeamAssessmentGradeRepository;
import com.fypgrading.adminservice.service.dto.TeamDTO;
import com.fypgrading.adminservice.service.enums.AssessmentEnum;
import com.fypgrading.adminservice.service.enums.RoleEnum;
import com.fypgrading.adminservice.service.event.EvaluationSubmittedEvent;
import com.fypgrading.adminservice.service.event.GradeFinalizedEvent;
import com.fypgrading.adminservice.service.mapper.TeamMapper;
import com.rabbitmq.client.Channel;
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
    private final TeamAssessmentGradeRepository teamAssessmentGradeRepository;
    private final EventDispatcher eventDispatcher;
    private final GradeRepository gradeRepository;
    private final ReviewerService reviewerService;
    private final RabbitService rabbitService;
    private final ObjectMapper objectMapper;
    private final TeamService teamService;
    private final TeamMapper teamMapper;

    public EventHandler(TeamAssessmentGradeRepository teamAssessmentGradeRepository,
                        EventDispatcher eventDispatcher,
                        GradeRepository gradeRepository,
                        ReviewerService reviewerService,
                        RabbitService rabbitService,
                        ObjectMapper objectMapper,
                        TeamService teamService,
                        TeamMapper teamMapper
    ) {
        this.teamAssessmentGradeRepository = teamAssessmentGradeRepository;
        this.eventDispatcher = eventDispatcher;
        this.gradeRepository = gradeRepository;
        this.reviewerService = reviewerService;
        this.rabbitService = rabbitService;
        this.objectMapper = objectMapper;
        this.teamService = teamService;
        this.teamMapper = teamMapper;
    }

    @Override
    public void onMessage(Message message, Channel channel) {
        try {
            logger.info("Received message: " + message);

            LocalDateTime currentTime = LocalDateTime.now();

            EvaluationSubmittedEvent event =
                    objectMapper.readValue(
                            new String(message.getBody()),
                            EvaluationSubmittedEvent.class
                    );
            TeamDTO team = event.getTeam();
            Team teamEntity = teamMapper.toEntity(team);

            logger.info("Received EvaluationSubmittedEvent: " + event);

            RoleEnum assessmentResponsibleRole = event.getAssessment().getResponsibleRole();

            //number of reviewers for this team assessment
            long teamReviewersCount =
                    reviewerService.countReviewersByTeamIdAndRoleName(
                            team.getId(),
                            assessmentResponsibleRole.name()
                    );

            logger.info("teamReviewersCount: " + teamReviewersCount);

            // number of submitted reviews for this team assessment
            long submittedAssessmentTeamReviewsCount =
                    gradeRepository.countByAssessmentAndReviewerTeam_TeamId(
                            event.getAssessment(), team.getId()
                    );

            logger.info("submittedAssessmentTeamReviewsCount: " + submittedAssessmentTeamReviewsCount);

            if (teamReviewersCount != submittedAssessmentTeamReviewsCount)
                return;

            // if all reviewers submitted their reviews for this team,
            // calculate final grade for this assessment, save it, and send notification
            float teamAssessmentGrade =
                    (float) (double) gradeRepository.findALlByReviewerTeam_TeamIdAndAssessment(
                                    team.getId(), event.getAssessment())
                            .parallelStream()
                            .reduce(0.0, (acc, gradeUnit) ->
                                    acc + (double) gradeUnit.getGrade(), Double::sum) / teamReviewersCount;

            logger.info("teamAssessmentGrade: " + teamAssessmentGrade);

            teamAssessmentGradeRepository.save(
                new TeamAssessmentGrade(
                    teamEntity, event.getAssessment(), teamAssessmentGrade
                )
            );

            logger.info("Saved teamAssessmentGrade: " + teamAssessmentGrade);

            GradeFinalizedEvent assessmentGradeFinalizedEvent =
                    new GradeFinalizedEvent(team, event.getAssessment(), currentTime);
            eventDispatcher.sendAdminNotification(assessmentGradeFinalizedEvent);

            logger.info("Sent GradeFinalizedEvent: " + assessmentGradeFinalizedEvent);

            // total number of finally graded assessments for this team
            List<TeamAssessmentGrade> gradedAssessments =
                    teamAssessmentGradeRepository.findAllByTeamId(team.getId());

            if (gradedAssessments.size() != AssessmentEnum.values().length) {
                return;
            }

            // if all assessments are finally graded,
            // calculate final grade for this team, save it, and send notification

            //TODO: Not to divide by 4... what are percentages for each assessment?

            float finalGrade =
                    (float) (double) gradedAssessments
                            .parallelStream()
                            .reduce(0.0, (acc, gradedAssessment) ->
                                    acc + (double) gradedAssessment.getGrade(), Double::sum) / 4;

            teamService.saveFinalGrade(teamEntity, finalGrade);

            GradeFinalizedEvent finalGradeFinalizedEvent =
                    new GradeFinalizedEvent(team, null, currentTime);
            eventDispatcher.sendAdminNotification(finalGradeFinalizedEvent);

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        } catch (Exception ex) {
            rabbitService.sendNack(message, channel);
        }
    }
}

