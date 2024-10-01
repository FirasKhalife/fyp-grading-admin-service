package com.fypgrading.adminservice.controller;

import com.fypgrading.adminservice.service.GradeService;
import com.fypgrading.adminservice.service.TeamService;
import com.fypgrading.adminservice.service.dto.GradeDTO;
import com.fypgrading.adminservice.service.dto.GradedEvaluationDTO;
import com.fypgrading.adminservice.service.dto.TeamGradesDTO;
import com.fypgrading.adminservice.service.enums.SystemRoleEnum;
import jakarta.annotation.security.RolesAllowed;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RolesAllowed({ SystemRoleEnum.Names.ROLE_ADMIN })
@RequestMapping("/api/admin")
public class AdminController {

    private final GradeService gradeService;
    private final TeamService teamService;

    @GetMapping("/teams/grades")
    public ResponseEntity<List<TeamGradesDTO>> getAllTeamsGrades() {
        List<TeamGradesDTO> teams = teamService.getAllTeamsGrades();
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
