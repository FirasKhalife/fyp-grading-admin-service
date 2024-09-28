package com.fypgrading.adminservice.service;

import com.fypgrading.adminservice.config.security.SecurityUtils;
import com.fypgrading.adminservice.entity.Assessment;
import com.fypgrading.adminservice.entity.Grade;
import com.fypgrading.adminservice.entity.Reviewer;
import com.fypgrading.adminservice.entity.TeamReviewer;
import com.fypgrading.adminservice.repository.AssessmentRepository;
import com.fypgrading.adminservice.repository.ReviewerRepository;
import com.fypgrading.adminservice.repository.RoleRepository;
import com.fypgrading.adminservice.service.client.NotificationClient;
import com.fypgrading.adminservice.service.dto.*;
import com.fypgrading.adminservice.service.mapper.AssessmentMapper;
import com.fypgrading.adminservice.service.mapper.ReviewerMapper;
import com.fypgrading.adminservice.service.mapper.TeamMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@AllArgsConstructor
@Service
public class ReviewerService {

    private final NotificationClient notificationClient;
    private final AssessmentRepository assessmentRepository;
    private final TeamReviewerService teamReviewerService;
    private final ReviewerRepository reviewerRepository;
    private final AssessmentMapper assessmentMapper;
    private final RoleRepository roleRepository;
    private final ReviewerMapper reviewerMapper;
    private final TeamMapper teamMapper;

    public List<ReviewerDTO> getReviewers() {
        List<Reviewer> reviewers = reviewerRepository.findAll();
        return reviewerMapper.toDTOList(reviewers);
    }

    public ReviewerDTO createReviewer(ReviewerLoginDTO reviewerLoginDTO) {
        Reviewer reviewer = reviewerMapper.toEntity(reviewerLoginDTO);
        Reviewer createdEntity = reviewerRepository.save(reviewer);
        return reviewerMapper.toDTO(createdEntity);
    }

    public ReviewerDTO updateReviewer(UUID id, ReviewerLoginDTO reviewerLoginDTO) {
        getReviewerById(id);
        Reviewer reviewer = reviewerMapper.toEntity(reviewerLoginDTO);
        reviewer.setId(id);
        Reviewer updatedEntity = reviewerRepository.save(reviewer);
        return reviewerMapper.toDTO(updatedEntity);
    }

    public ReviewerDTO deleteReviewer(UUID id) {
        Reviewer reviewer = getReviewerById(id);
        reviewerRepository.delete(reviewer);
        return reviewerMapper.toDTO(reviewer);
    }

    public Reviewer getReviewerById(UUID id) {
        return reviewerRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Reviewer not found"));
    }

    public ReviewerDTO getReviewerViewById(UUID id) {
        Reviewer reviewer = getReviewerById(id);
        return reviewerMapper.toDTO(reviewer);
    }

    public ReviewerTeamsAssessmentsDTO getReviewerTeamsAssessments(UUID id) {
        List<TeamReviewer> teamReviewers = teamReviewerService.getReviewerTeamListByReviewerId(id);

        List<ReviewerTeamViewDTO> resList = new ArrayList<>();

        List<AssessmentDTO> reviewerAssessments = assessmentMapper.toDTOList(
                assessmentRepository.findAllByRoleInOrderById(roleRepository.getDistinctReviewerRoles(id))
        );

        //for every team
        for(TeamReviewer teamReviewer : teamReviewers) {
            List<Assessment> allAssessments =
                    teamReviewer.getReviewerRoles().stream().flatMap(role -> role.getAssessments().stream())
                            .sorted(Comparator.comparing(Assessment::getId))
                            .toList();

            //returning corresponding assessments with grades for each team
            List<TeamReviewerAssessmentDTO> teamReviewerAssessmentList = new ArrayList<>();
            allAssessments.forEach(assessment -> {
                TeamReviewerAssessmentDTO teamReviewerAssessmentDTO =
                        new TeamReviewerAssessmentDTO(new AssessmentDTO(assessment));

                List<Grade> gradeEntityList =
                        teamReviewer.getGrades().stream()
                                .filter(grade -> grade.getAssessment().equals(assessment)).toList();

                if (gradeEntityList.isEmpty()) {
                    teamReviewerAssessmentDTO.setGrade(null);
                } else {
                    teamReviewerAssessmentDTO.setGrade(gradeEntityList.get(0).getGrade());
                }

                teamReviewerAssessmentList.add(teamReviewerAssessmentDTO);
            });

            ReviewerTeamViewDTO reviewerTeamViewDTO = new ReviewerTeamViewDTO();
            reviewerTeamViewDTO.setTeam(teamMapper.toDTO(teamReviewer.getTeam()));
            reviewerTeamViewDTO.setTeamAssessments(teamReviewerAssessmentList);

            resList.add(reviewerTeamViewDTO);
        }

        return new ReviewerTeamsAssessmentsDTO(reviewerAssessments, resList);
    }

    public long countReviewersByTeamIdAndAssessmentId(Long teamId, Long assessmentId) {
        return reviewerRepository.countReviewersByTeamIdAndAssessmentId(teamId, assessmentId);
    }

    public Reviewer getById(UUID id) {
        return reviewerRepository.findById(id)
            .orElseThrow(EntityNotFoundException::new);
    }

    public Optional<Reviewer> findById(UUID id) {
        return reviewerRepository.findById(id);
    }

    public Reviewer save(Reviewer reviewer) {
        return reviewerRepository.save(reviewer);
    }

    public Reviewer createReviewerFromAuthentication(Map<String, Object> claims) {
        return Reviewer.builder()
            .id(UUID.fromString((String) claims.get("sub")))
            .email((String) claims.get("email"))
            .firstName((String) claims.get("given_name"))
            .lastName((String) claims.get("family_name"))
            .isAdmin(SecurityUtils.isAdminInClaims(claims))
            .build();
    }

    public List<NotificationDTO> getAdminNotifications(UUID reviewerId) {
        Optional<Reviewer> reviewerOpt = reviewerRepository.findById(reviewerId);
        if (reviewerOpt.isEmpty() || !reviewerOpt.get().getIsAdmin()) {
            return Collections.emptyList();
        }

        return notificationClient.getNotifications();
    }

    public ReviewerHomeDTO getReviewerHome(UUID reviewerId) {
        return new ReviewerHomeDTO(getReviewerTeamsAssessments(reviewerId), getAdminNotifications(reviewerId));
    }
}
