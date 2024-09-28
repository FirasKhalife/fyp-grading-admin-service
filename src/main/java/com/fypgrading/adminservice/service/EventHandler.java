package com.fypgrading.adminservice.service;

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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Service
public class EventHandler {

    @PersistenceContext
    private final EntityManager entityManager;
    private final TeamAssessmentRepository teamAssessmentRepository;
    private final AssessmentMapper assessmentMapper;
    private final EventDispatcher eventDispatcher;
    private final GradeRepository gradeRepository;
    private final ReviewerService reviewerService;
    private final TeamService teamService;
    private final TeamMapper teamMapper;

    @Bean
    public Consumer<EvaluationSubmittedEvent> checkForNotification() {
        return event -> {
            TeamDTO teamDTO = event.getTeam();

            //number of reviewers for this team assessment
            long teamReviewersCount =
                reviewerService.countReviewersByTeamIdAndAssessmentId(
                    teamDTO.getId(), event.getAssessment().getId()
                );

            // number of submitted reviews for this team assessment
            long submittedAssessmentTeamReviewsCount =
                gradeRepository.countByTeamReviewer_TeamIdAndAssessmentId(
                    teamDTO.getId(), event.getAssessment().getId()
                );

            if (teamReviewersCount != submittedAssessmentTeamReviewsCount)
                return;

            // if all reviewers submitted their reviews for this team assessment,
            // calculate final grade for this assessment, save it, and send notification
            float teamAssessmentGrade = (float) (double)
                gradeRepository.findAllByTeamReviewer_TeamIdAndAssessmentId(
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
        };
    }
}
