package com.fypgrading.adminservice.service;

import com.fypgrading.adminservice.entity.Reviewer;
import com.fypgrading.adminservice.entity.ReviewerRole;
import com.fypgrading.adminservice.entity.Team;
import com.fypgrading.adminservice.entity.TeamReviewer;
import com.fypgrading.adminservice.repository.ReviewerRepository;
import com.fypgrading.adminservice.repository.ReviewerRoleRepository;
import com.fypgrading.adminservice.repository.TeamReviewerRepository;
import com.fypgrading.adminservice.repository.TeamRepository;
import com.fypgrading.adminservice.service.dto.ReviewerDTO;
import com.fypgrading.adminservice.service.dto.ReviewerRolesDTO;
import com.fypgrading.adminservice.service.dto.TeamDTO;
import com.fypgrading.adminservice.service.dto.TeamReviewerRolesDTO;
import com.fypgrading.adminservice.service.mapper.ReviewerMapper;
import com.fypgrading.adminservice.service.mapper.ReviewerRoleMapper;
import com.fypgrading.adminservice.service.mapper.TeamMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class TeamReviewerService {

    private final TeamReviewerRepository teamReviewerRepository;
    private final ReviewerRepository reviewerRepository;
    private final ReviewerMapper reviewerMapper;
    private final ReviewerRoleRepository reviewerRoleRepository;
    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;
    private final ReviewerRoleMapper reviewerRoleMapper;

    public TeamReviewer createReviewerTeam(UUID reviewerId, Long teamId) {
        Reviewer reviewer = reviewerRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        TeamReviewer teamReviewer = new TeamReviewer(reviewer, team);

        return teamReviewerRepository.save(teamReviewer);
    }

    public TeamReviewerRolesDTO getTeamReviewerRoles(UUID reviewerId, Long teamId) {
        List<ReviewerRole> reviewerRoles = reviewerRoleRepository.getReviewerTeamRoles(reviewerId, teamId);

        return new TeamReviewerRolesDTO(reviewerId, teamId, reviewerRoleMapper.toEnumList(reviewerRoles));
    }

    public TeamReviewer getReviewerTeamById(UUID reviewerId, Long teamId) {
        return teamReviewerRepository.findByReviewerIdAndTeamId(reviewerId, teamId)
                .orElseThrow(() -> new RuntimeException("ReviewerTeam not found"));
    }

    public List<TeamReviewer> getReviewerTeamListByReviewerId(UUID reviewerId) {
        return teamReviewerRepository.findByReviewerId(reviewerId);
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
        List<ReviewerRole> roles = reviewerRoleRepository.getReviewerRolesByReviewerId(reviewerId);

        return new ReviewerRolesDTO(reviewerId, reviewerRoleMapper.toEnumList(roles));
    }
}
