package com.fypgrading.adminservice.service;

import com.fypgrading.adminservice.entity.*;
import com.fypgrading.adminservice.entity.idClass.ReviewerTeamId;
import com.fypgrading.adminservice.repository.GradeRepository;
import com.fypgrading.adminservice.repository.ReviewerTeamRepository;
import com.fypgrading.adminservice.service.dto.EvaluationDTO;
import com.fypgrading.adminservice.service.dto.TeamDTO;
import com.fypgrading.adminservice.service.dto.TeamGradeDTO;
import com.fypgrading.adminservice.service.enums.AssessmentEnum;
import com.fypgrading.adminservice.service.event.EvaluationSubmittedEvent;
import com.fypgrading.adminservice.service.mapper.GradeMapper;
import com.fypgrading.adminservice.service.mapper.TeamMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class GradeService {

    private final com.fypgrading.adminservice.service.EventDispatcher eventDispatcher;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ReviewerTeamRepository reviewerTeamRepository;
    private final ReviewerService reviewerService;
    private final GradeRepository gradeRepository;
    private final TeamService teamService;
    private final GradeMapper gradeMapper;
    private final TeamMapper teamMapper;

    public GradeService(
            ReviewerTeamRepository reviewerTeamRepository,
            GradeRepository gradeRepository,
            EventDispatcher eventDispatcher,
            ReviewerService reviewerService,
            GradeMapper gradeMapper,
            TeamService teamService,
            TeamMapper teamMapper
    ) {
        this.reviewerTeamRepository = reviewerTeamRepository;
        this.gradeRepository = gradeRepository;
        this.eventDispatcher = eventDispatcher;
        this.reviewerService = reviewerService;
        this.gradeMapper = gradeMapper;
        this.teamService = teamService;
        this.teamMapper = teamMapper;
    }

    public List<TeamGradeDTO> getGradesByAssessment(String assessmentStr) {
        AssessmentEnum assessment = AssessmentEnum.valueOf(assessmentStr.toUpperCase());
        List<Grade> reviewerTeamGrades = gradeRepository.findAllByAssessment(assessment);
        return gradeMapper.toTeamGradeDTOList(reviewerTeamGrades);
    }

    public List<EvaluationDTO> getTeamEvaluationsByAssessment(String assessment, Integer teamId) {
        ResponseEntity<List<EvaluationDTO>> teamEvaluations = restTemplate.exchange(
                "http://localhost:8082/api/evaluations/" + assessment + "/" + teamId,
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {}
        );

        return teamEvaluations.getBody();
    }

    public TeamGradeDTO submitGrade(TeamGradeDTO teamGrade) {

        Reviewer reviewer = reviewerService.getReviewerById(teamGrade.getReviewerId());
        Team team = teamService.getTeamById(teamGrade.getTeamId());
        TeamDTO teamDTO = teamMapper.toDTO(team);

        ReviewerTeam reviewerTeam =
                reviewerTeamRepository.findById(new ReviewerTeamId(reviewer, team))
                        .orElseThrow(() -> new IllegalArgumentException("Reviewer and Team are not linked!"));

        gradeRepository.findByAssessmentAndReviewerTeam_ReviewerIdAndReviewerTeam_TeamId(
                teamGrade.getAssessment(), teamGrade.getReviewerId(), teamGrade.getTeamId()
        ).ifPresent(grade -> {
            throw new IllegalStateException("Reviewer already submitted evaluation for this team assessment!");
        });

        Grade grade = new Grade(teamGrade.getAssessment(), teamGrade.getGrade(), reviewerTeam);

        Grade createdGrade = gradeRepository.save(grade);

        EvaluationSubmittedEvent event = new EvaluationSubmittedEvent(teamDTO, teamGrade.getAssessment());
        eventDispatcher.checkForAdminNotification(event);

        return gradeMapper.toTeamGradeDTO(createdGrade);
    }

    public List<TeamGradeDTO> getGrades(Integer reviewerId, Integer teamId) {
        List<Grade> grades =
                gradeRepository
                        .findAllByReviewerTeam_ReviewerIdAndReviewerTeam_TeamId(
                reviewerId, teamId
        );

        return gradeMapper.toTeamGradeDTOList(grades);
    }
}
