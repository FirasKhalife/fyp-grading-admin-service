package com.fypgrading.adminservice.controller;

import com.fypgrading.adminservice.service.GradeService;
import com.fypgrading.adminservice.service.dto.EvaluationDTO;
import com.fypgrading.adminservice.service.dto.TeamGradeDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/grades")
public class AdminGradeController {

    private final GradeService gradeService;

    public AdminGradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @GetMapping("/{assessment}")
    public ResponseEntity<List<TeamGradeDTO>> getGradesByAssessment(@PathVariable String assessment) {
        List<TeamGradeDTO> teamGrade = gradeService.getGradesByAssessment(assessment);
        return ResponseEntity.ok().body(teamGrade);
    }

    @GetMapping("/{assessment}/{teamId}")
    public ResponseEntity<List<EvaluationDTO>> getTeamEvaluationsByAssessment(@PathVariable String assessment,
                                                                              @PathVariable Integer teamId) {
        List<EvaluationDTO> evaluations =
                gradeService.getTeamEvaluationsByAssessment(assessment, teamId);
        return ResponseEntity.ok().body(evaluations);
    }

}
