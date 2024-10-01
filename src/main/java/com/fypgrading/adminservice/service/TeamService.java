package com.fypgrading.adminservice.service;

import com.fypgrading.adminservice.entity.*;
import com.fypgrading.adminservice.repository.AssessmentRepository;
import com.fypgrading.adminservice.repository.TeamAssessmentRepository;
import com.fypgrading.adminservice.repository.TeamRepository;
import com.fypgrading.adminservice.service.dto.*;
import com.fypgrading.adminservice.service.mapper.ReviewerMapper;
import com.fypgrading.adminservice.service.mapper.TeamAssessmentMapper;
import com.fypgrading.adminservice.service.mapper.TeamMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class TeamService {

    private final TeamAssessmentRepository teamAssessmentRepository;
    private final AssessmentRepository assessmentRepository;
    private final TeamRepository teamRepository;

    private final TeamAssessmentMapper teamAssessmentMapper;
    private final ReviewerMapper reviewerMapper;
    private final TeamMapper teamMapper;

    public List<TeamDTO> getTeams() {
        List<Team> teams = teamRepository.findAll();
        return teamMapper.toDTOList(teams);
    }

    public TeamDTO createTeam(TeamDTO teamDTO) {
        Team team = teamMapper.toEntity(teamDTO);
        Team createdEntity = teamRepository.save(team);
        return teamMapper.toDTO(createdEntity);
    }

    public TeamDTO updateTeam(Long id, TeamDTO teamDTO) {
        getTeamById(id);
        Team team = teamMapper.toEntity(teamDTO);
        Team updatedEntity = teamRepository.save(team);
        return teamMapper.toDTO(updatedEntity);
    }

    public void saveFinalGrade(Team team, float grade) {
        team.setFinalGrade(grade);
        teamRepository.save(team);
    }

    public TeamDTO deleteTeam(Long id) {
        Team team = getTeamById(id);
        teamRepository.delete(team);
        return teamMapper.toDTO(team);
    }

    public Team getTeamById(Long id) {
        return teamRepository.findById(id).orElseThrow(() ->
            new EntityNotFoundException("Team not found"));
    }

    public CountDTO getTeamReviewersCount(Long id) {
        int reviewersCount = getTeamById(id).getReviewers().size();
        return new CountDTO(Integer.toUnsignedLong(reviewersCount));
    }

    public List<ReviewerDTO> getTeamReviewers(Long id) {
        List<Reviewer> reviewers =
            getTeamById(id).getReviewers()
                .stream().map(TeamReviewer::getReviewer)
                .toList();
        return reviewerMapper.toDTOList(reviewers);
    }

    public List<TeamGradesDTO> getAllTeamsGrades() {
        List<Team> teams = teamRepository.findAllByOrderByIdAsc();
        List<Assessment> allAssessments = assessmentRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));

        // team ID -> (assessment ID -> team assessment)
        Map<Long, Map<Long, TeamAssessment>> teamAssessmentsMap = teamAssessmentRepository.findAll()
            .stream().collect(
                Collectors.groupingBy(
                    teamAssessment -> teamAssessment.getTeam().getId(),
                    Collectors.toMap(
                        teamAssessment -> teamAssessment.getAssessment().getId(),
                        teamAssessment -> teamAssessment)
                ));

        return teams.stream().map(team -> {
            List<TeamAssessmentDTO> teamGradedAssessments =
                allAssessments.stream().map(assessment ->
                    teamAssessmentMapper.toDTO(
                        teamAssessmentsMap
                            .getOrDefault(team.getId(), Map.of())
                            .getOrDefault(assessment.getId(), new TeamAssessment(team, assessment, null)))
                ).toList();

            return new TeamGradesDTO(teamMapper.toDTO(team), teamGradedAssessments);
        }).toList();
    }
}
