package com.fypgrading.adminservice.controller;

import com.fypgrading.adminservice.service.GradeService;
import com.fypgrading.adminservice.service.dto.GradeDTO;
import com.fypgrading.adminservice.service.dto.GradeIDsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
public class GradeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final GradeService gradeService;

    public GradeController(GradeService gradeService) {

        this.gradeService = gradeService;
        LOGGER.info("GradeController initialized");
    }

    @PostMapping("/")
    public ResponseEntity<GradeDTO> submitGrade(@RequestBody GradeIDsDTO grade) {
        LOGGER.info("Submitting grade for teamId: {} and reviewerId: {}", grade.getTeamId(), grade.getReviewerId());
        GradeDTO createdGrade = gradeService.submitGrade(grade);
        LOGGER.info("Grade submitted successfully");
        return ResponseEntity.ok().body(createdGrade);
    }

    @GetMapping("/{reviewerId}/{teamId}")
    public ResponseEntity<List<GradeDTO>> getGrades(@PathVariable Integer reviewerId,
                                                    @PathVariable Integer teamId) {
        LOGGER.info("Getting grades for reviewerId: {} and teamId: {}", reviewerId, teamId);
        List<GradeDTO> grades = gradeService.getGrades(reviewerId, teamId);
        LOGGER.info("Found {} grades for reviewerId: {} and teamId: {}", grades.size(), reviewerId, teamId);
        return ResponseEntity.ok().body(grades);
    }

}
