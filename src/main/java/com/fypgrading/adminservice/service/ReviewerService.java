package com.fypgrading.adminservice.service;

import com.fypgrading.adminservice.entity.Assessment;
import com.fypgrading.adminservice.entity.Grade;
import com.fypgrading.adminservice.entity.Reviewer;
import com.fypgrading.adminservice.entity.ReviewerTeam;
import com.fypgrading.adminservice.repository.AssessmentRepository;
import com.fypgrading.adminservice.repository.ReviewerRepository;
import com.fypgrading.adminservice.repository.RoleRepository;
import com.fypgrading.adminservice.service.dto.*;
import com.fypgrading.adminservice.service.mapper.AssessmentMapper;
import com.fypgrading.adminservice.service.mapper.ReviewerMapper;
import com.fypgrading.adminservice.service.mapper.TeamMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class ReviewerService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final AssessmentRepository assessmentRepository;
    private final ReviewerTeamService reviewerTeamService;
    private final ReviewerRepository reviewerRepository;
    private final AssessmentMapper assessmentMapper;
    private final RoleRepository roleRepository;
    private final ReviewerMapper reviewerMapper;
    private final TeamMapper teamMapper;

    public ReviewerService(
            AssessmentRepository assessmentRepository,
            ReviewerTeamService reviewerTeamService,
            ReviewerRepository reviewerRepository,
            AssessmentMapper assessmentMapper,
            RoleRepository roleRepository,
            ReviewerMapper reviewerMapper,
            TeamMapper teamMapper
    ) {
        this.assessmentRepository = assessmentRepository;
        this.reviewerTeamService = reviewerTeamService;
        this.reviewerRepository = reviewerRepository;
        this.assessmentMapper = assessmentMapper;
        this.roleRepository = roleRepository;
        this.reviewerMapper = reviewerMapper;
        this.teamMapper = teamMapper;
    }

    public List<ReviewerDTO> getReviewers() {
        List<Reviewer> reviewers = reviewerRepository.findAll();
        return reviewerMapper.toDTOList(reviewers);
    }

    public ReviewerDTO createReviewer(ReviewerLoginDTO reviewerLoginDTO) {
        Reviewer reviewer = reviewerMapper.toEntity(reviewerLoginDTO);
        Reviewer createdEntity = reviewerRepository.save(reviewer);
        return reviewerMapper.toDTO(createdEntity);
    }

    public ReviewerDTO updateReviewer(Integer id, ReviewerLoginDTO reviewerLoginDTO) {
        getReviewerById(id);
        Reviewer reviewer = reviewerMapper.toEntity(reviewerLoginDTO);
        reviewer.setId(id);
        Reviewer updatedEntity = reviewerRepository.save(reviewer);
        return reviewerMapper.toDTO(updatedEntity);
    }

    public ReviewerDTO deleteReviewer(Integer id) {
        Reviewer reviewer = getReviewerById(id);
        reviewerRepository.delete(reviewer);
        return reviewerMapper.toDTO(reviewer);
    }

    public Reviewer getReviewerById(Integer id) {
        return reviewerRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Reviewer not found"));
    }

    public ReviewerDTO getReviewerViewById(Integer id) {
        Reviewer reviewer = getReviewerById(id);
        return reviewerMapper.toDTO(reviewer);
    }

    public ReviewerTeamsAssessmentsDTO getReviewerTeamsAssessments(Integer id) {
        List<ReviewerTeam> reviewerTeams = reviewerTeamService.getReviewerTeamListByReviewerId(id);

        List<ReviewerTeamViewDTO> resList = new ArrayList<>();

        List<AssessmentDTO> reviewerAssessments = assessmentMapper.toDTOList(
                assessmentRepository.findAllByRoleInOrderById(roleRepository.getDistinctReviewerRoles(id))
        );

        //for every team
        for(ReviewerTeam reviewerTeam : reviewerTeams) {
            List<Assessment> allAssessments =
                    reviewerTeam.getReviewerRoles().stream().flatMap(role -> role.getAssessments().stream())
                            .sorted(Comparator.comparing(Assessment::getId))
                            .toList();

            //returning corresponding assessments with grades for each team
            List<TeamReviewerAssessmentDTO> teamReviewerAssessmentList = new ArrayList<>();
            allAssessments.forEach(assessment -> {
                TeamReviewerAssessmentDTO teamReviewerAssessmentDTO =
                        new TeamReviewerAssessmentDTO(new AssessmentDTO(assessment));

                List<Grade> gradeEntityList =
                        reviewerTeam.getGrades().stream()
                                .filter(grade -> grade.getAssessment().equals(assessment)).toList();

                if (gradeEntityList.isEmpty()) {
                    teamReviewerAssessmentDTO.setGrade(null);
                } else {
                    teamReviewerAssessmentDTO.setGrade(gradeEntityList.get(0).getGrade());
                }

                teamReviewerAssessmentList.add(teamReviewerAssessmentDTO);
            });

            ReviewerTeamViewDTO reviewerTeamViewDTO = new ReviewerTeamViewDTO();
            reviewerTeamViewDTO.setTeam(teamMapper.toDTO(reviewerTeam.getTeam()));
            reviewerTeamViewDTO.setTeamAssessments(teamReviewerAssessmentList);

            resList.add(reviewerTeamViewDTO);
        }

        return new ReviewerTeamsAssessmentsDTO(reviewerAssessments, resList);
    }

    public long countReviewersByTeamIdAndAssessmentId(Integer teamId, Integer assessmentId) {
        return reviewerRepository.countReviewersByTeamIdAndAssessmentId(teamId, assessmentId);
    }

    public List<NotificationDTO> getAdminNotifications(Integer reviewerId) {
        Optional<Reviewer> reviewerOpt = reviewerRepository.findById(reviewerId);
        if (reviewerOpt.isEmpty() || !reviewerOpt.get().getIsAdmin()) {
            return Collections.emptyList();
        }

        ResponseEntity<List<NotificationDTO>> notificationsResponse = restTemplate.exchange(
                "http://localhost:8084/api/notifications/",
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {}
        );

        if (notificationsResponse.getStatusCode().isError()) {
            throw new IllegalStateException("Error while fetching notifications");
        }

        List<NotificationDTO> notifications = notificationsResponse.getBody();

        assert notifications != null;
        return notifications;
    }

    public ReviewerHomeDTO getReviewerHome(Integer reviewerId) {
        return new ReviewerHomeDTO(getReviewerTeamsAssessments(reviewerId), getAdminNotifications(reviewerId));
    }
}
