package com.fypgrading.adminservice.controller;

import com.fypgrading.adminservice.service.GradeService;
import com.fypgrading.adminservice.service.dto.TeamGradeDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
public class ReviewerGradeController {

    private final GradeService gradeService;

    public ReviewerGradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @PostMapping("/")
    public ResponseEntity<TeamGradeDTO> submitGrade(@RequestBody TeamGradeDTO grade) {
        TeamGradeDTO createdGrade = gradeService.submitGrade(grade);
        return ResponseEntity.ok().body(createdGrade);
    }

    @GetMapping("/{reviewerId}/{teamId}")
    public ResponseEntity<List<TeamGradeDTO>> getGrades(@PathVariable Integer reviewerId,
                                                        @PathVariable Integer teamId) {
        List<TeamGradeDTO> grades = gradeService.getGrades(reviewerId, teamId);
        return ResponseEntity.ok().body(grades);
    }

}
