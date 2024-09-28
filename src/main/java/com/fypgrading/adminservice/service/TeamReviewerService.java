package com.fypgrading.adminservice.service;

import com.fypgrading.adminservice.entity.Reviewer;
import com.fypgrading.adminservice.entity.Role;
import com.fypgrading.adminservice.entity.Team;
import com.fypgrading.adminservice.entity.TeamReviewer;
import com.fypgrading.adminservice.repository.ReviewerRepository;
import com.fypgrading.adminservice.repository.ReviewerTeamRepository;
import com.fypgrading.adminservice.repository.RoleRepository;
import com.fypgrading.adminservice.repository.TeamRepository;
import com.fypgrading.adminservice.service.dto.ReviewerDTO;
import com.fypgrading.adminservice.service.dto.ReviewerRolesDTO;
import com.fypgrading.adminservice.service.dto.TeamDTO;
import com.fypgrading.adminservice.service.dto.TeamReviewerRolesDTO;
import com.fypgrading.adminservice.service.enums.RoleEnum;
import com.fypgrading.adminservice.service.mapper.ReviewerMapper;
import com.fypgrading.adminservice.service.mapper.RoleMapper;
import com.fypgrading.adminservice.service.mapper.TeamMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class TeamReviewerService {

    private final ReviewerTeamRepository reviewerTeamRepository;
    private final ReviewerRepository reviewerRepository;
    private final ReviewerMapper reviewerMapper;
    private final RoleRepository roleRepository;
    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;
    private final RoleMapper roleMapper;

    public TeamReviewer createReviewerTeam(UUID reviewerId, Long teamId) {
        Reviewer reviewer = reviewerRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        TeamReviewer teamReviewer = new TeamReviewer(reviewer, team);

        return reviewerTeamRepository.save(teamReviewer);
    }

    public TeamReviewerRolesDTO getTeamReviewerRoles(UUID reviewerId, Long teamId) {
        List<Role> reviewerRoles = roleRepository.getReviewerTeamRoles(reviewerId, teamId);

        return new TeamReviewerRolesDTO(reviewerId, teamId, roleMapper.toEnumList(reviewerRoles));
    }

    public TeamReviewer getReviewerTeamById(UUID reviewerId, Long teamId) {
        return reviewerTeamRepository.findByReviewerIdAndTeamId(reviewerId, teamId)
                .orElseThrow(() -> new RuntimeException("ReviewerTeam not found"));
    }

    public List<TeamReviewer> getReviewerTeamListByReviewerId(UUID reviewerId) {
        return reviewerTeamRepository.findByReviewerId(reviewerId);
    }

    public List<TeamDTO> getReviewerTeams(UUID reviewerId) {
        List<Team> teams = teamRepository.getAllReviewerTeams(reviewerId);

        return teamMapper.toDTOList(teams);
    }

    public List<ReviewerDTO> getTeamReviewers(Long teamId) {
        List<Reviewer> reviewers = reviewerRepository.findAllTeamReviewers(teamId);

        return reviewerMapper.toDTOList(reviewers);
    }

    public ReviewerRolesDTO getReviewerRoles(UUID reviewerId) {
        List<Role> roles = roleRepository.getDistinctReviewerRoles(reviewerId);

        return new ReviewerRolesDTO(reviewerId, roleMapper.toEnumList(roles));
    }

    public Reviewer addReviewerTeamRole(UUID id, Long teamId, RoleEnum role) {
        TeamReviewer teamReviewer = getReviewerTeamById(id, teamId);

        Role reviewerRole = roleRepository.findById(role.getInstanceId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        teamReviewer.getReviewerRoles().add(reviewerRole);

        return reviewerRepository.save(teamReviewer.getReviewer());
    }
}
