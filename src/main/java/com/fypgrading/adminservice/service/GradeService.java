package com.fypgrading.adminservice.service;

import com.fypgrading.adminservice.entity.*;
import com.fypgrading.adminservice.repository.GradeRepository;
import com.fypgrading.adminservice.repository.TeamReviewerRepository;
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

import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class GradeService {

    private final TeamReviewerRepository teamReviewerRepository;
    private final GradeRepository gradeRepository;

    private final AssessmentService assessmentService;
    private final ReviewerService reviewerService;
    private final TeamService teamService;

    private final AssessmentMapper assessmentMapper;
    private final ReviewerMapper reviewerMapper;
    private final GradeMapper gradeMapper;
    private final TeamMapper teamMapper;

    private final EventDispatcher eventDispatcher;
    private final EvaluationClient evaluationClient;

    public List<GradeDTO> getGradesByAssessment(String assessmentStr) {
        Assessment assessment = assessmentService.getAssessmentByLowerCaseName(assessmentStr);
        List<Grade> reviewerTeamGrades = gradeRepository.findAllByAssessmentId(assessment.getId());
        return gradeMapper.toTeamGradeDTOList(reviewerTeamGrades);
    }

    public List<GradedEvaluationDTO> getTeamEvaluationsByAssessment(String assessmentStr, Long teamId) {
        Team team = teamService.getTeamById(teamId);

        List<EvaluationDTO> teamEvaluations = evaluationClient.getTeamEvaluationsByAssessment(assessmentStr, teamId);

        Assessment assessment = assessmentService.getAssessmentByLowerCaseName(assessmentStr);
        List<Grade> assessmentGrades = gradeRepository.findAllByTeamReviewer_TeamIdAndAssessmentId(teamId, assessment.getId());
        List<Reviewer> assessmentReviewers =
            teamReviewerRepository.findByTeamIdAndAssessmentId(teamId, assessment.getId())
                .stream().map(TeamReviewer::getReviewer).toList();

        // reviewer -> grade
        Map<Reviewer, Float> reviewerGradeMap = assessmentGrades.stream()
            .collect(Collectors.toMap(
                grade -> grade.getTeamReviewer().getReviewer(),
                Grade::getGrade));

        // reviewer ID -> graded rubrics
        Map<UUID, List<GradedRubricDTO>> reviewerIdGradedRubricsMap = teamEvaluations.stream()
            .collect(Collectors.toMap(
                EvaluationDTO::getReviewerId,
                EvaluationDTO::getGradedRubrics));

        return assessmentReviewers.stream().map(reviewer ->
            new GradedEvaluationDTO(
                    reviewerMapper.toDTO(reviewer),
                    teamMapper.toDTO(team),
                    assessmentMapper.toDTO(assessment),
                    reviewerIdGradedRubricsMap.getOrDefault(reviewer.getId(), List.of()),
                    reviewerGradeMap.get(reviewer))).toList();
    }

    public GradeDTO submitGrade(GradeIdDTO gradeIdDTO) {

        Assessment assessment = assessmentService.getAssessmentByUpperCaseName(gradeIdDTO.getAssessment());
        Reviewer reviewer = reviewerService.getReviewerById(gradeIdDTO.getReviewerId());
        Team team = teamService.getTeamById(gradeIdDTO.getTeamId());
        TeamDTO teamDTO = teamMapper.toDTO(team);

        TeamReviewer teamReviewer =
                teamReviewerRepository.findByReviewerIdAndTeamId(reviewer.getId(), team.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Reviewer and Team are not linked!"));

        gradeRepository.findByTeamReviewer_ReviewerIdAndTeamReviewer_TeamIdAndAssessmentId(
                gradeIdDTO.getReviewerId(), gradeIdDTO.getTeamId(), assessment.getId()
        ).ifPresent(foundGrade -> {
            throw new IllegalStateException("Reviewer already submitted evaluation for this team assessment!");
        });

        Grade grade = new Grade(assessment, gradeIdDTO.getGrade(), teamReviewer);
        Grade createdGrade = gradeRepository.save(grade);

        EvaluationSubmittedEvent event = new EvaluationSubmittedEvent(teamDTO, assessmentMapper.toDTO(assessment));
        eventDispatcher.checkForAdminNotification(event);

        return gradeMapper.toTeamGradeDTO(createdGrade);
    }

    public List<GradeDTO> getTeamReviewerGrades(UUID reviewerId, Long teamId) {
        List<Grade> grades =
                gradeRepository.findAllByTeamReviewer_ReviewerIdAndTeamReviewer_TeamId(reviewerId, teamId);
        return gradeMapper.toTeamGradeDTOList(grades);
    }

    public List<GradeDTO> getTeamGradesByAssessment(String assessmentStr, Long teamId) {
        AssessmentEnum assessment = AssessmentEnum.valueOf(assessmentStr.toUpperCase());
        List<Grade> grades =
                gradeRepository.findAllByTeamReviewer_TeamIdAndAssessmentId(teamId, assessment.getInstanceId());
        return gradeMapper.toTeamGradeDTOList(grades);
    }
}
