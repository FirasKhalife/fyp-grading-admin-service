package com.fypgrading.adminservice.controller;

import com.fypgrading.adminservice.service.GradeService;
import com.fypgrading.adminservice.service.TeamService;
import com.fypgrading.adminservice.service.dto.GradeDTO;
import com.fypgrading.adminservice.service.dto.GradedEvaluationDTO;
import com.fypgrading.adminservice.service.dto.TeamGradesDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);

    private final GradeService gradeService;
    private final TeamService teamService;

    public AdminController(GradeService gradeService, TeamService teamService) {
        this.gradeService = gradeService;
        this.teamService = teamService;
        LOGGER.info("AdminController initialized!");
    }

    @GetMapping("/teams/grades")
    public ResponseEntity<List<TeamGradesDTO>> getAllTeamsGrades() {
        LOGGER.info("Getting all teams' grades");
        List<TeamGradesDTO> teams = teamService.getAllGrades();
        LOGGER.info("Found {} teams", teams.size());
        return ResponseEntity.ok().body(teams);
    }

    @GetMapping("/grades/{assessment}")
    public ResponseEntity<List<GradeDTO>> getGradesByAssessment(@PathVariable String assessment) {
        LOGGER.info("Getting grades for assessment: {}", assessment);
        List<GradeDTO> teamGrade = gradeService.getGradesByAssessment(assessment);
        LOGGER.info("Found {} grades for assessment {}", teamGrade.size(), assessment);
        return ResponseEntity.ok().body(teamGrade);
    }

    @GetMapping("/teams/{teamId}/grades/{assessment}")
    public ResponseEntity<List<GradeDTO>> getTeamGradesByAssessment(@PathVariable Integer teamId,
                                                                    @PathVariable String assessment) {
        LOGGER.info("Getting grades for teamId: {} and assessment: {}", teamId, assessment);
        List<GradeDTO> reviewerTeamGrades = gradeService.getTeamGradesByAssessment(assessment, teamId);
        LOGGER.info("Found {} grades for teamId: {} and assessment: {}", reviewerTeamGrades.size(), teamId, assessment);
        return ResponseEntity.ok().body(reviewerTeamGrades);
    }

    @GetMapping("/teams/{teamId}/evaluations/{assessment}")
    public ResponseEntity<List<GradedEvaluationDTO>> getTeamEvaluationsByAssessment(@PathVariable Integer teamId,
                                                                                    @PathVariable String assessment) {
        LOGGER.info("Getting evaluations for teamId: {} and assessment: {}", teamId, assessment);
        List<GradedEvaluationDTO> evaluations = gradeService.getTeamEvaluationsByAssessment(assessment, teamId);
        LOGGER.info("Found {} evaluations for teamId: {} and assessment: {}", evaluations.size(), teamId, assessment);
        return ResponseEntity.ok().body(evaluations);
    }
}
