package com.fypgrading.adminservice.service;

import com.fypgrading.adminservice.entity.*;
import com.fypgrading.adminservice.repository.GradeRepository;
import com.fypgrading.adminservice.repository.ReviewerTeamRepository;
import com.fypgrading.adminservice.service.client.EvaluationClient;
import com.fypgrading.adminservice.service.dto.*;
import com.fypgrading.adminservice.service.enums.AssessmentEnum;
import com.fypgrading.adminservice.service.event.EvaluationSubmittedEvent;
import com.fypgrading.adminservice.service.mapper.AssessmentMapper;
import com.fypgrading.adminservice.service.mapper.GradeMapper;
import com.fypgrading.adminservice.service.mapper.ReviewerMapper;
import com.fypgrading.adminservice.service.mapper.TeamMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class GradeService {

    private final com.fypgrading.adminservice.service.EventDispatcher eventDispatcher;
    private final ReviewerTeamRepository reviewerTeamRepository;
    private final EvaluationClient evaluationClient;
    private final AssessmentService assessmentService;
    private final AssessmentMapper assessmentMapper;
    private final ReviewerService reviewerService;
    private final GradeRepository gradeRepository;
    private final ReviewerMapper reviewerMapper;
    private final TeamService teamService;
    private final GradeMapper gradeMapper;
    private final TeamMapper teamMapper;

    public List<GradeDTO> getGradesByAssessment(String assessmentStr) {
        AssessmentEnum assessment = AssessmentEnum.valueOf(assessmentStr.toUpperCase());
        List<Grade> reviewerTeamGrades = gradeRepository.findAllByAssessmentId(assessment.getInstanceId());
        return gradeMapper.toTeamGradeDTOList(reviewerTeamGrades);
    }

    public void testCheckNotification() {
        eventDispatcher.checkForAdminNotification(
            new EvaluationSubmittedEvent(null, null)
        );
    }

    public List<GradedEvaluationDTO> getTeamEvaluationsByAssessment(String assessmentStr, Long teamId) {
        List<EvaluationDTO> teamEvaluations =
            evaluationClient.getTeamEvaluationsByAssessment(assessmentStr, teamId);

        Assessment assessment = assessmentService.getAssessmentByLowerCaseName(assessmentStr);

        List<Grade> gradeList =
                gradeRepository.findAllByTeamReviewer_TeamIdAndAssessmentId(teamId, assessment.getId());

        List<TeamReviewer> teamReviewers =
                reviewerTeamRepository.findByTeamIdAndAssessmentId(teamId, assessment.getId());

        System.out.println("teamReviewers: " + teamReviewers);

        if (teamReviewers.isEmpty()) {
            throw new IllegalStateException("Team reviewers not found!");
        }

        Team team = teamReviewers.get(0).getTeam();
        List<Reviewer> reviewers = teamReviewers.stream().map(TeamReviewer::getReviewer).toList();

        List<GradedEvaluationDTO> evaluations = new ArrayList<>();
        reviewers.stream().map(reviewer -> {
            Grade grade = gradeList.stream().filter(g -> g.getTeamReviewer().getReviewer().equals(reviewer)).findFirst().orElse(null);
            EvaluationDTO evaluation = teamEvaluations.stream().filter(e -> e.getReviewerId().equals(reviewer.getId())).findFirst().orElse(null);
            return new GradedEvaluationDTO(
                    reviewerMapper.toDTO(reviewer),
                    teamMapper.toDTO(team),
                    assessmentMapper.toDTO(assessment),
                    evaluation != null ? evaluation.getGradedRubrics() : Collections.emptyList(),
                    grade != null ? grade.getGrade() : null
            );
        }).forEach(evaluations::add);

        return evaluations;
    }

    public GradeDTO submitGrade(GradeIDsDTO gradeIDs) {

        Assessment assessment = assessmentService.getAssessmentByUpperCaseName(gradeIDs.getAssessment());
        Reviewer reviewer = reviewerService.getReviewerById(gradeIDs.getReviewerId());
        Team team = teamService.getTeamById(gradeIDs.getTeamId());
        TeamDTO teamDTO = teamMapper.toDTO(team);

        TeamReviewer teamReviewer =
                reviewerTeamRepository.findByReviewerIdAndTeamId(reviewer.getId(), team.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Reviewer and Team are not linked!"));

        gradeRepository.findByTeamReviewer_ReviewerIdAndTeamReviewer_TeamIdAndAssessmentId(
                gradeIDs.getReviewerId(), gradeIDs.getTeamId(), assessment.getId()
        ).ifPresent(foundGrade -> {
            throw new IllegalStateException("Reviewer already submitted evaluation for this team assessment!");
        });

        Grade grade = new Grade(assessment, gradeIDs.getGrade(), teamReviewer);

        Grade createdGrade = gradeRepository.save(grade);

        EvaluationSubmittedEvent event = new EvaluationSubmittedEvent(teamDTO, assessmentMapper.toDTO(assessment));
        eventDispatcher.checkForAdminNotification(event);

        return gradeMapper.toTeamGradeDTO(createdGrade);
    }

    public List<GradeDTO> getGrades(UUID reviewerId, Long teamId) {
        List<Grade> grades =
                gradeRepository.findAllByTeamReviewer_ReviewerIdAndTeamReviewer_TeamId(
                        reviewerId, teamId
                );

        return gradeMapper.toTeamGradeDTOList(grades);
    }

    public List<GradeDTO> getTeamGradesByAssessment(String assessmentStr, Long teamId) {
        AssessmentEnum assessment = AssessmentEnum.valueOf(assessmentStr.toUpperCase());
        List<Grade> grades =
                gradeRepository.findAllByTeamReviewer_TeamIdAndAssessmentId(teamId, assessment.getInstanceId());

        return gradeMapper.toTeamGradeDTOList(grades);
    }
}
