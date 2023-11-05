package com.fypgrading.administration.service;

import com.fypgrading.administration.entity.Team;
import com.fypgrading.administration.repository.TeamRepository;
import com.fypgrading.administration.service.dto.TeamDTO;
import com.fypgrading.administration.service.mapper.TeamMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;

    public TeamService(TeamRepository teamRepository, TeamMapper teamMapper) {
        this.teamRepository = teamRepository;
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

    public TeamDTO deleteTeam(Integer id) {
        Team team = getTeamById(id);
        teamRepository.delete(team);
        return teamMapper.toDTO(team);
    }

    private Team getTeamById(Integer id) {
        return teamRepository.findById(id).orElseThrow(() ->
                        new EntityNotFoundException("Team not found"));
    }
}
