package com.fypgrading.adminservice.service;

import com.fypgrading.adminservice.entity.Assessment;
import com.fypgrading.adminservice.entity.Grade;
import com.fypgrading.adminservice.entity.Reviewer;
import com.fypgrading.adminservice.entity.TeamReviewer;
import com.fypgrading.adminservice.exception.AuthException;
import com.fypgrading.adminservice.repository.AssessmentRepository;
import com.fypgrading.adminservice.repository.ReviewerRepository;
import com.fypgrading.adminservice.repository.RoleRepository;
import com.fypgrading.adminservice.service.dto.*;
import com.fypgrading.adminservice.service.mapper.AssessmentMapper;
import com.fypgrading.adminservice.service.mapper.ReviewerMapper;
import com.fypgrading.adminservice.service.mapper.TeamMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class ReviewerService {

    private final AssessmentRepository assessmentRepository;
    private final TeamReviewerService teamReviewerService;
    private final ReviewerRepository reviewerRepository;
    private final AssessmentMapper assessmentMapper;
    private final RoleRepository roleRepository;
    private final ReviewerMapper reviewerMapper;
    private final RestTemplate restTemplate;
    private final TeamMapper teamMapper;

    public ReviewerService(
            AssessmentRepository assessmentRepository,
            TeamReviewerService teamReviewerService,
            ReviewerRepository reviewerRepository,
            AssessmentMapper assessmentMapper,
            RoleRepository roleRepository,
            ReviewerMapper reviewerMapper,
            RestTemplate restTemplate,
            TeamMapper teamMapper
    ) {
        this.assessmentRepository = assessmentRepository;
        this.teamReviewerService = teamReviewerService;
        this.reviewerRepository = reviewerRepository;
        this.assessmentMapper = assessmentMapper;
        this.roleRepository = roleRepository;
        this.reviewerMapper = reviewerMapper;
        this.restTemplate = restTemplate;
        this.teamMapper = teamMapper;
    }

    public JwtResponseDTO login(LoginDTO loginDTO) {
        Reviewer reviewer = reviewerRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new AuthException("Invalid credentials"));

        if (!loginDTO.getPassword().equals(reviewer.getPassword()))
            throw new AuthException("Invalid credentials");

        JwtResponseDTO jwtResponse = new JwtResponseDTO(reviewer);
        jwtResponse.setRoles(teamReviewerService.getReviewerRoles(reviewer.getId()).getRoles());

        return jwtResponse;
    }

    public ReviewerSignupDTO signup(ReviewerSignupDTO reviewerInfo) {
        Optional<Reviewer> reviewerOpt = reviewerRepository.findByEmail(reviewerInfo.getEmail());
        if (reviewerOpt.isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        Reviewer reviewer = new Reviewer(
                reviewerInfo.getFirstName(),
                reviewerInfo.getLastName(),
                reviewerInfo.getEmail(),
                reviewerInfo.getPassword(),
                reviewerInfo.isAdmin()
        );

        Reviewer createdReviewer = reviewerRepository.save(reviewer);

        reviewerInfo.getTeamRoleList().forEach(teamRoleDTO -> {
            teamReviewerService.createReviewerTeam(createdReviewer.getId(), teamRoleDTO.getTeamId());
            teamReviewerService.addReviewerTeamRole(reviewer.getId(), teamRoleDTO.getTeamId(), teamRoleDTO.getRole());
        });

        return reviewerInfo;
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

    public ReviewerDTO updateReviewer(Long id, ReviewerLoginDTO reviewerLoginDTO) {
        getReviewerById(id);
        Reviewer reviewer = reviewerMapper.toEntity(reviewerLoginDTO);
        reviewer.setId(id);
        Reviewer updatedEntity = reviewerRepository.save(reviewer);
        return reviewerMapper.toDTO(updatedEntity);
    }

    public ReviewerDTO deleteReviewer(Long id) {
        Reviewer reviewer = getReviewerById(id);
        reviewerRepository.delete(reviewer);
        return reviewerMapper.toDTO(reviewer);
    }

    public Reviewer getReviewerById(Long id) {
        return reviewerRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Reviewer not found"));
    }

    public ReviewerDTO getReviewerViewById(Long id) {
        Reviewer reviewer = getReviewerById(id);
        return reviewerMapper.toDTO(reviewer);
    }

    public ReviewerTeamsAssessmentsDTO getReviewerTeamsAssessments(Long id) {
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

    public List<NotificationDTO> getAdminNotifications(Long reviewerId) {
        Optional<Reviewer> reviewerOpt = reviewerRepository.findById(reviewerId);
        if (reviewerOpt.isEmpty() || !reviewerOpt.get().getIsAdmin()) {
            return Collections.emptyList();
        }

        NotificationListDTO notificationList = restTemplate.getForObject(
                "http://api-gateway/api/notifications/", NotificationListDTO.class
        );

        if (notificationList == null) {
            throw new IllegalStateException("Error while fetching notifications");
        }

        List<NotificationDTO> notifications = notificationList.getNotifications();

        assert notifications != null;
        return notifications;
    }

    public ReviewerHomeDTO getReviewerHome(Long reviewerId) {
        return new ReviewerHomeDTO(getReviewerTeamsAssessments(reviewerId), getAdminNotifications(reviewerId));
    }
}
