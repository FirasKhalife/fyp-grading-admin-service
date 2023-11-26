package com.fypgrading.adminservice.controller;

import com.fypgrading.adminservice.service.GradeService;
import com.fypgrading.adminservice.service.dto.GradeDTO;
import com.fypgrading.adminservice.service.dto.GradeIDsDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
public class GradeController {

    private final GradeService gradeService;

    public GradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @PostMapping("/")
    public ResponseEntity<GradeDTO> submitGrade(@RequestBody GradeIDsDTO grade) {
        GradeDTO createdGrade = gradeService.submitGrade(grade);
        return ResponseEntity.ok().body(createdGrade);
    }

    @GetMapping("/{reviewerId}/{teamId}")
    public ResponseEntity<List<GradeDTO>> getGrades(@PathVariable Integer reviewerId,
                                                    @PathVariable Integer teamId) {
        List<GradeDTO> grades = gradeService.getGrades(reviewerId, teamId);
        return ResponseEntity.ok().body(grades);
    }

}
