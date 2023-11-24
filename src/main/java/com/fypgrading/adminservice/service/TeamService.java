package com.fypgrading.adminservice.service;

import com.fypgrading.adminservice.entity.Reviewer;
import com.fypgrading.adminservice.entity.ReviewerTeam;
import com.fypgrading.adminservice.entity.Team;
import com.fypgrading.adminservice.repository.TeamRepository;
import com.fypgrading.adminservice.service.dto.CountDTO;
import com.fypgrading.adminservice.service.dto.ReviewerViewDTO;
import com.fypgrading.adminservice.service.dto.TeamDTO;
import com.fypgrading.adminservice.service.mapper.ReviewerMapper;
import com.fypgrading.adminservice.service.mapper.TeamMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final ReviewerMapper reviewerMapper;
    private final TeamMapper teamMapper;

    public TeamService(TeamRepository teamRepository,
                       ReviewerMapper reviewerMapper,
                       TeamMapper teamMapper
    ) {
        this.teamRepository = teamRepository;
        this.reviewerMapper = reviewerMapper;
        this.teamMapper = teamMapper;
    }

    public List<TeamDTO> getTeams() {
        List<Team> teams = teamRepository.findAll();
        return teamMapper.toDTOList(teams);
    }

    public TeamDTO createTeam(TeamDTO teamDTO) {
        Team team = teamMapper.toEntity(teamDTO);
        Team createdEntity = teamRepository.save(team);
        return teamMapper.toDTO(createdEntity);
    }

    public TeamDTO updateTeam(Integer id, TeamDTO teamDTO) {
        getTeamById(id);
        Team team = teamMapper.toEntity(teamDTO);
        Team updatedEntity = teamRepository.save(team);
        return teamMapper.toDTO(updatedEntity);
    }

    public void saveFinalGrade(Team team, float grade) {
        team.setFinalGrade(grade);
        teamRepository.save(team);
    }

    public TeamDTO deleteTeam(Integer id) {
        Team team = getTeamById(id);
        teamRepository.delete(team);
        return teamMapper.toDTO(team);
    }

    public Team getTeamById(Integer id) {
        return teamRepository.findById(id).orElseThrow(() ->
                        new EntityNotFoundException("Team not found"));
    }

    public CountDTO getTeamReviewersCount(Integer id) {
        int reviewersCount = getTeamById(id).getTeamReviewers().size();
        return new CountDTO(Integer.toUnsignedLong(reviewersCount));
    }

    public List<ReviewerViewDTO> getTeamReviewers(Integer id) {
        List<Reviewer> reviewers =
                getTeamById(id).getTeamReviewers()
                        .parallelStream()
                        .map(ReviewerTeam::getReviewer)
                        .toList();
        return reviewerMapper.toViewDTOList(reviewers);
    }
}
