package com.fypgrading.adminservice.service;

import com.fypgrading.adminservice.config.security.SecurityUtils;
import com.fypgrading.adminservice.entity.*;
import com.fypgrading.adminservice.repository.AssessmentRepository;
import com.fypgrading.adminservice.repository.ReviewerRepository;
import com.fypgrading.adminservice.repository.ReviewerRoleRepository;
import com.fypgrading.adminservice.service.client.NotificationClient;
import com.fypgrading.adminservice.service.dto.*;
import com.fypgrading.adminservice.service.enums.SystemRoleEnum;
import com.fypgrading.adminservice.service.mapper.AssessmentMapper;
import com.fypgrading.adminservice.service.mapper.ReviewerMapper;
import com.fypgrading.adminservice.service.mapper.TeamMapper;
import jakarta.annotation.security.RolesAllowed;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.*;

@AllArgsConstructor
@Service
public class ReviewerService {

    private final ReviewerRoleRepository reviewerRoleRepository;
    private final AssessmentRepository assessmentRepository;
    private final ReviewerRepository reviewerRepository;

    private final TeamReviewerService teamReviewerService;
    private final SystemRoleService systemRoleService;

    private final AssessmentMapper assessmentMapper;
    private final ReviewerMapper reviewerMapper;
    private final TeamMapper teamMapper;

    private final NotificationClient notificationClient;

    public List<ReviewerDTO> getAllReviewers() {
        List<Reviewer> reviewers = reviewerRepository.findAll();
        return reviewerMapper.toDTOList(reviewers);
    }

    public Reviewer saveAuthorities(Reviewer reviewer, Collection<GrantedAuthority> authorities) {
        List<String> authorityNames = authorities.stream().map(GrantedAuthority::getAuthority).toList();
        List<SystemRole> roles = systemRoleService.findAllByNames(authorityNames);
        reviewer.setRoles(roles);
        return reviewerRepository.save(reviewer);
    }

    public Reviewer getReviewerById(UUID id) {
        return reviewerRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Reviewer not found"));
    }

    public ReviewerDTO getReviewerViewById(UUID id) {
        Reviewer reviewer = getReviewerById(id);
        return reviewerMapper.toDTO(reviewer);
    }

    public ReviewerTeamsAssessmentsDTO getReviewerTeamsAssessments(UUID reviewerId) {
        List<ReviewerRole> reviewerRoles = reviewerRoleRepository.getReviewerRolesByReviewerId(reviewerId);
        List<AssessmentDTO> reviewerAssessments = assessmentMapper.toDTOList(
                assessmentRepository.findAllByReviewerRoleInOrderById(reviewerRoles));

        List<TeamReviewer> reviewerTeams = teamReviewerService.getReviewerTeamListByReviewerId(reviewerId);
        List<TeamGradedAssessmentsDTO> teamsGradedAssessments =
            reviewerTeams.stream().map(reviewerTeam -> {
                List<Assessment> reviewerTeamAssessments = reviewerTeam.getReviewerRoles()
                    .stream().flatMap(role -> role.getAssessments().stream())
                    .sorted(Comparator.comparing(Assessment::getId))
                    .toList();

                List<GradedAssessmentDTO> teamGradedAssessments =
                    reviewerTeamAssessments.stream().map(assessment -> {
                        Optional<Grade> grade = reviewerTeam.getGrades()
                            .stream().filter(teamGrade -> Objects.equals(teamGrade.getAssessment(), assessment))
                            .findFirst();

                        return new GradedAssessmentDTO(
                                assessmentMapper.toDTO(assessment),
                                grade.map(Grade::getGrade).orElse(null));
                    }).toList();

                return new TeamGradedAssessmentsDTO(teamMapper.toDTO(reviewerTeam.getTeam()), teamGradedAssessments);
            }).toList();

        return new ReviewerTeamsAssessmentsDTO(reviewerAssessments, teamsGradedAssessments);
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

    public Reviewer createReviewerFromAuthentication(Map<String, Object> claims) {
        return Reviewer.builder()
            .id(UUID.fromString((String) claims.get("sub")))
            .email((String) claims.get("email"))
            .firstName((String) claims.get("given_name"))
            .lastName((String) claims.get("family_name"))
            .build();
    }

    @RolesAllowed({ SystemRoleEnum.Names.ROLE_ADMIN })
    public List<NotificationDTO> getNotifications() {
        return notificationClient.getNotifications();
    }

    public ReviewerHomeDTO getReviewerHome() {
        Reviewer reviewer = SecurityUtils.getCurrentUser();
        ReviewerTeamsAssessmentsDTO assessments = getReviewerTeamsAssessments(reviewer.getId());
        List<NotificationDTO> notifications =
            SecurityUtils.isUserAdmin(reviewer) ? getNotifications() : List.of();
        return new ReviewerHomeDTO(assessments, notifications);
    }
}
