package com.fypgrading.adminservice.service;

import com.fypgrading.adminservice.entity.AssessmentGrade;
import com.fypgrading.adminservice.entity.EvaluationSubmissionDTO;
import com.fypgrading.adminservice.entity.ReviewerTeam;
import com.fypgrading.adminservice.repository.AssessmentGradeRepository;
import com.fypgrading.adminservice.repository.ReviewerRepository;
import com.fypgrading.adminservice.repository.ReviewerTeamRepository;
import com.fypgrading.adminservice.repository.TeamRepository;
import com.fypgrading.adminservice.service.dto.EvaluationDTO;
import com.fypgrading.adminservice.service.dto.TeamGradeDTO;
import com.fypgrading.adminservice.service.enums.AssessmentEnum;
import com.fypgrading.adminservice.service.mapper.ReviewerTeamMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Service
public class GradeService {

    private final AssessmentGradeRepository assessmentGradeRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ReviewerTeamRepository reviewerTeamRepository;
    private final ReviewerRepository reviewerRepository;
    private final ReviewerTeamMapper gradeMapper;
    private final TeamRepository teamRepository;

    public GradeService(ReviewerTeamMapper gradeMapper, AssessmentGradeRepository assessmentGradeRepository, ReviewerTeamRepository reviewerTeamRepository, TeamRepository teamRepository, ReviewerRepository reviewerRepository) {
        this.assessmentGradeRepository = assessmentGradeRepository;
        this.reviewerTeamRepository = reviewerTeamRepository;
        this.reviewerRepository = reviewerRepository;
        this.teamRepository = teamRepository;
        this.gradeMapper = gradeMapper;
    }

    public List<TeamGradeDTO> getGradesByAssessment(String assessmentStr) {
        AssessmentEnum assessment = AssessmentEnum.valueOf(assessmentStr.toUpperCase());
        List<AssessmentGrade> reviewerTeamGrades = assessmentGradeRepository.findAllByAssessment(assessment);
        return gradeMapper.toTeamGradeDTO(reviewerTeamGrades);
    }

    public List<EvaluationDTO> getTeamEvaluationsByAssessment(String assessment, Integer teamId) {
        ResponseEntity<List<EvaluationDTO>> teamEvaluations = restTemplate.exchange(
                "http://localhost:8082/api/evaluations/" + assessment + "/" + teamId,
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {}
        );

        return teamEvaluations.getBody();
    }

    public void submitGrade(EvaluationSubmissionDTO evalSub) {
        if (!teamRepository.existsById(evalSub.getTeamId())
            || !reviewerRepository.existsById((evalSub.getReviewerId()))) {
            throw new IllegalArgumentException("Arguments not found in DB");
        }

        Optional<AssessmentGrade> assessmentGradeOpt = assessmentGradeRepository
                .findByAssessmentAndReviewerTeam_ReviewerIdAndReviewerTeam_TeamId(
                        evalSub.getAssessment(), evalSub.getReviewerId(), evalSub.getTeamId()
                );

        if (assessmentGradeOpt.isPresent()) {
            throw new IllegalStateException("Reviewer already submitted evaluation for this team assessment!");
        }

        ReviewerTeam reviewerTeam = new ReviewerTeam(evalSub.getReviewerId(), evalSub.getTeamId());
        AssessmentGrade assessmentGrade = new AssessmentGrade(
                evalSub.getAssessment(), evalSub.getGrade(), reviewerTeam
        );

        assessmentGradeRepository.save(assessmentGrade);

        {
            long teamReviewersCount = reviewerTeamRepository.countByTeamId(evalSub.getTeamId());
            long submittedAssessmentTeamReviewsCount =
                    assessmentGradeRepository.countByAssessmentAndReviewerTeam_TeamId(
                            evalSub.getAssessment(), evalSub.getTeamId()
                    );
            if (teamReviewersCount != submittedAssessmentTeamReviewsCount)
                return;

            long allSubmittedTeamReviewsCount =
                    assessmentGradeRepository.countByReviewerTeam_TeamId(evalSub.getTeamId());

            if (allSubmittedTeamReviewsCount != teamReviewersCount * AssessmentEnum.values().length) {
//                eventDispatcher.sendAdminNotification(teamId, );
            }
        }
    }
}
