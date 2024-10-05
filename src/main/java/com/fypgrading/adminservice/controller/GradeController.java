package com.fypgrading.adminservice.controller;

import com.fypgrading.adminservice.service.GradeService;
import com.fypgrading.adminservice.service.dto.GradeDTO;
import com.fypgrading.adminservice.service.dto.GradeIdDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/api/grades")
public class GradeController {

    private final GradeService gradeService;

    @PostMapping("/")
    public ResponseEntity<GradeDTO> submitGrade(@RequestBody GradeIdDTO grade) {
        GradeDTO createdGrade = gradeService.submitGrade(grade);
        return ResponseEntity.ok().body(createdGrade);
    }

    @GetMapping("/{reviewerId}/{teamId}")
    public ResponseEntity<List<GradeDTO>> getTeamReviewerGrades(@PathVariable UUID reviewerId,
                                                                @PathVariable Long teamId) {
        List<GradeDTO> grades = gradeService.getTeamReviewerGrades(reviewerId, teamId);
        return ResponseEntity.ok().body(grades);
    }

}
