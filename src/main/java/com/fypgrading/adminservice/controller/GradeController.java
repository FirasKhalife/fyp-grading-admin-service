package com.fypgrading.adminservice.controller;

import com.fypgrading.adminservice.service.GradeService;
import com.fypgrading.adminservice.service.dto.GradeDTO;
import com.fypgrading.adminservice.service.dto.GradeIDsDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/grades")
public class GradeController {

    private final GradeService gradeService;

    @PostMapping("/")
    public ResponseEntity<GradeDTO> submitGrade(@RequestBody GradeIDsDTO grade) {
        GradeDTO createdGrade = gradeService.submitGrade(grade);
        return ResponseEntity.ok().body(createdGrade);
    }

    @GetMapping("/{reviewerId}/{teamId}")
    public ResponseEntity<List<GradeDTO>> getGrades(@PathVariable Long reviewerId,
                                                    @PathVariable Long teamId) {
        List<GradeDTO> grades = gradeService.getGrades(reviewerId, teamId);
        return ResponseEntity.ok().body(grades);
    }

}
