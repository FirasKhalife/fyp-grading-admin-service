package com.fypgrading.adminservice.controller;

import com.fypgrading.adminservice.service.GradeService;
import com.fypgrading.adminservice.service.TeamService;
import com.fypgrading.adminservice.service.dto.GradeDTO;
import com.fypgrading.adminservice.service.dto.GradedEvaluationDTO;
import com.fypgrading.adminservice.service.dto.TeamGradesDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final GradeService gradeService;
    private final TeamService teamService;

    public AdminController(GradeService gradeService, TeamService teamService) {
        this.gradeService = gradeService;
        this.teamService = teamService;
    }

    @GetMapping("/teams/grades")
    public ResponseEntity<List<TeamGradesDTO>> getAllTeamsGrades() {
        List<TeamGradesDTO> teams = teamService.getAllGrades();
        return ResponseEntity.ok().body(teams);
    }

    @GetMapping("/grades/{assessment}")
    public ResponseEntity<List<GradeDTO>> getGradesByAssessment(@PathVariable String assessment) {
        List<GradeDTO> teamGrade = gradeService.getGradesByAssessment(assessment);
        return ResponseEntity.ok().body(teamGrade);
    }

    @GetMapping("/teams/{teamId}/grades/{assessment}")
    public ResponseEntity<List<GradeDTO>> getTeamGradesByAssessment(@PathVariable Integer teamId,
                                                                    @PathVariable String assessment) {
        List<GradeDTO> reviewerTeamGrades =
                gradeService.getTeamGradesByAssessment(assessment, teamId);
        return ResponseEntity.ok().body(reviewerTeamGrades);
    }

    @GetMapping("/teams/{teamId}/evaluations/{assessment}")
    public ResponseEntity<List<GradedEvaluationDTO>> getTeamEvaluationsByAssessment(@PathVariable() Integer teamId,
                                                                                    @PathVariable() String assessment) {
        List<GradedEvaluationDTO> evaluations =
                gradeService.getTeamEvaluationsByAssessment(assessment, teamId);
        return ResponseEntity.ok().body(evaluations);
    }
}
