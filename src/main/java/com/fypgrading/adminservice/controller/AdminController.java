package com.fypgrading.adminservice.controller;

import com.fypgrading.adminservice.service.GradeService;
import com.fypgrading.adminservice.service.TeamService;
import com.fypgrading.adminservice.service.dto.GradeDTO;
import com.fypgrading.adminservice.service.dto.GradedEvaluationDTO;
import com.fypgrading.adminservice.service.dto.TeamGradesDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RefreshScope
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final GradeService gradeService;
    private final TeamService teamService;
    private final String buildVersion;

    public AdminController(
        GradeService gradeService,
        TeamService teamService,
        @Value("${build.version}") String buildVersion
    ) {
        this.gradeService = gradeService;
        this.teamService = teamService;
        this.buildVersion = buildVersion;
    }

    @PostMapping("testCheckNotification")
    public ResponseEntity<Void> testCheckNotification() {
        gradeService.testCheckNotification();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/build-version")
    public ResponseEntity<String> getBuildVersion() {
        return ResponseEntity.ok().body(buildVersion);
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
    public ResponseEntity<List<GradeDTO>> getTeamGradesByAssessment(@PathVariable Long teamId,
                                                                    @PathVariable String assessment) {
        List<GradeDTO> assessmentTeamGrades = gradeService.getTeamGradesByAssessment(assessment, teamId);
        return ResponseEntity.ok().body(assessmentTeamGrades);
    }

    @GetMapping("/teams/{teamId}/evaluations/{assessment}")
    public ResponseEntity<List<GradedEvaluationDTO>> getTeamEvaluationsByAssessment(@PathVariable Long teamId,
                                                                                    @PathVariable String assessment) {
        List<GradedEvaluationDTO> evaluations = gradeService.getTeamEvaluationsByAssessment(assessment, teamId);
        return ResponseEntity.ok().body(evaluations);
    }
}
