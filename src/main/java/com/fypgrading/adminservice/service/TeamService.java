package com.fypgrading.adminservice.service;

import com.fypgrading.adminservice.entity.*;
import com.fypgrading.adminservice.repository.AssessmentRepository;
import com.fypgrading.adminservice.repository.TeamAssessmentRepository;
import com.fypgrading.adminservice.repository.TeamRepository;
import com.fypgrading.adminservice.service.dto.*;
import com.fypgrading.adminservice.service.mapper.ReviewerMapper;
import com.fypgrading.adminservice.service.mapper.TeamMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TeamService {

    private final TeamAssessmentRepository teamAssessmentRepository;
    private final AssessmentRepository assessmentRepository;
    private final TeamRepository teamRepository;
    private final ReviewerMapper reviewerMapper;
    private final TeamMapper teamMapper;



    public TeamService(TeamAssessmentRepository teamAssessmentRepository,
                       AssessmentRepository assessmentRepository,
                       TeamRepository teamRepository,
                       ReviewerMapper reviewerMapper,
                       TeamMapper teamMapper
    ) {
        this.teamAssessmentRepository = teamAssessmentRepository;
        this.assessmentRepository = assessmentRepository;
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
        int reviewersCount = getTeamById(id).getReviewers().size();
        return new CountDTO(Integer.toUnsignedLong(reviewersCount));
    }

    public List<ReviewerDTO> getTeamReviewers(Integer id) {
        List<Reviewer> reviewers =
                getTeamById(id).getReviewers()
                        .parallelStream()
                        .map(ReviewerTeam::getReviewer)
                        .toList();
        return reviewerMapper.toDTOList(reviewers);
    }

    public List<TeamGradesDTO> getAllGrades() {
        List<Team> teams = teamRepository.findAllByOrderByIdAsc();

        List<Assessment> assessments = assessmentRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));

        return teams.stream().map(team -> {
            List<TeamAssessment> grades = teamAssessmentRepository.findAllByTeamId(team.getId());

            List<TeamAssessmentDTO> gradesDTO = new ArrayList<>();
            assessments.forEach(assessment -> {
                TeamAssessment grade = new TeamAssessment(team, assessment, null);
                int index;
                if ((index = grades.indexOf(grade)) != -1) {
                    grade = grades.get(index);
                }

                gradesDTO.add(new TeamAssessmentDTO(grade));
            });

            return new TeamGradesDTO(teamMapper.toDTO(team), gradesDTO);
        }).toList();
    }
}
