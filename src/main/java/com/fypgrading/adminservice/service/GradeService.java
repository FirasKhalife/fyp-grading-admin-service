package com.fypgrading.adminservice.service;

import com.fypgrading.adminservice.entity.*;
import com.fypgrading.adminservice.entity.idClass.ReviewerTeamId;
import com.fypgrading.adminservice.repository.GradeRepository;
import com.fypgrading.adminservice.repository.ReviewerTeamRepository;
import com.fypgrading.adminservice.service.dto.*;
import com.fypgrading.adminservice.service.enums.AssessmentEnum;
import com.fypgrading.adminservice.service.event.EvaluationSubmittedEvent;
import com.fypgrading.adminservice.service.mapper.AssessmentMapper;
import com.fypgrading.adminservice.service.mapper.GradeMapper;
import com.fypgrading.adminservice.service.mapper.ReviewerMapper;
import com.fypgrading.adminservice.service.mapper.TeamMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class GradeService {

    private final com.fypgrading.adminservice.service.EventDispatcher eventDispatcher;
    private final ReviewerTeamRepository reviewerTeamRepository;
    private final AssessmentService assessmentService;
    private final AssessmentMapper assessmentMapper;
    private final ReviewerService reviewerService;
    private final GradeRepository gradeRepository;
    private final ReviewerMapper reviewerMapper;
    private final RestTemplate restTemplate;
    private final TeamService teamService;
    private final GradeMapper gradeMapper;
    private final TeamMapper teamMapper;

    public GradeService(
            ReviewerTeamRepository reviewerTeamRepository,
            AssessmentService assessmentService,
            AssessmentMapper assessmentMapper,
            GradeRepository gradeRepository,
            EventDispatcher eventDispatcher,
            ReviewerService reviewerService,
            ReviewerMapper reviewerMapper,
            RestTemplate restTemplate,
            GradeMapper gradeMapper,
            TeamService teamService,
            TeamMapper teamMapper
    ) {
        this.reviewerTeamRepository = reviewerTeamRepository;
        this.assessmentService = assessmentService;
        this.assessmentMapper = assessmentMapper;
        this.gradeRepository = gradeRepository;
        this.eventDispatcher = eventDispatcher;
        this.reviewerService = reviewerService;
        this.reviewerMapper = reviewerMapper;
        this.restTemplate = restTemplate;
        this.gradeMapper = gradeMapper;
        this.teamService = teamService;
        this.teamMapper = teamMapper;
    }

    public List<GradeDTO> getGradesByAssessment(String assessmentStr) {
        AssessmentEnum assessment = AssessmentEnum.valueOf(assessmentStr.toUpperCase());
        List<Grade> reviewerTeamGrades = gradeRepository.findAllByAssessmentId(assessment.getEnumId());
        return gradeMapper.toTeamGradeDTOList(reviewerTeamGrades);
    }

    public List<GradedEvaluationDTO> getTeamEvaluationsByAssessment(String assessmentStr, Integer teamId) {
        EvaluationDTOList teamEvaluationList = restTemplate.getForObject(
                "http://api-gateway/api/evaluations/" + assessmentStr + "/" + teamId, EvaluationDTOList.class
        );

        if (teamEvaluationList == null) {
            throw new IllegalStateException("Team evaluations not found!");
        }
        List<EvaluationDTO> teamEvaluations = teamEvaluationList.getEvaluations();

        Assessment assessment = assessmentService.getAssessmentByLowerCaseName(assessmentStr);

        List<Grade> gradeList =
                gradeRepository.findAllByReviewerTeam_TeamIdAndAssessmentId(teamId, assessment.getId());

        List<ReviewerTeam> teamReviewers =
                reviewerTeamRepository.findByTeamIdAndAssessmentId(teamId, assessment.getId());

        System.out.println("teamReviewers: " + teamReviewers);

        if (teamReviewers.isEmpty()) {
            throw new IllegalStateException("Team reviewers not found!");
        }

        Team team = teamReviewers.get(0).getTeam();
        List<Reviewer> reviewers = teamReviewers.stream().map(ReviewerTeam::getReviewer).toList();

        List<GradedEvaluationDTO> evaluations = new ArrayList<>();
        reviewers.stream().map(reviewer -> {
            Grade grade = gradeList.stream().filter(g -> g.getReviewerTeam().getReviewer().equals(reviewer)).findFirst().orElse(null);
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

        ReviewerTeam reviewerTeam =
                reviewerTeamRepository.findById(new ReviewerTeamId(reviewer, team))
                        .orElseThrow(() -> new IllegalArgumentException("Reviewer and Team are not linked!"));

        gradeRepository.findByReviewerTeam_ReviewerIdAndReviewerTeam_TeamIdAndAssessmentId(
                gradeIDs.getReviewerId(), gradeIDs.getTeamId(), assessment.getId()
        ).ifPresent(foundGrade -> {
            throw new IllegalStateException("Reviewer already submitted evaluation for this team assessment!");
        });

        Grade grade = new Grade(assessment, gradeIDs.getGrade(), reviewerTeam);

        Grade createdGrade = gradeRepository.save(grade);

        EvaluationSubmittedEvent event = new EvaluationSubmittedEvent(teamDTO, assessmentMapper.toDTO(assessment));
        eventDispatcher.checkForAdminNotification(event);

        return gradeMapper.toTeamGradeDTO(createdGrade);
    }

    public List<GradeDTO> getGrades(Integer reviewerId, Integer teamId) {
        List<Grade> grades =
                gradeRepository.findAllByReviewerTeam_ReviewerIdAndReviewerTeam_TeamId(
                        reviewerId, teamId
                );

        return gradeMapper.toTeamGradeDTOList(grades);
    }

    public List<GradeDTO> getTeamGradesByAssessment(String assessmentStr, Integer teamId) {
        AssessmentEnum assessment = AssessmentEnum.valueOf(assessmentStr.toUpperCase());
        List<Grade> grades =
                gradeRepository.findAllByReviewerTeam_TeamIdAndAssessmentId(teamId, assessment.getEnumId());

        return gradeMapper.toTeamGradeDTOList(grades);
    }
}
