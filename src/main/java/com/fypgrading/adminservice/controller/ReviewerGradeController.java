package com.fypgrading.adminservice.controller;

import com.fypgrading.adminservice.entity.EvaluationSubmissionDTO;
import com.fypgrading.adminservice.service.GradeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/grades/")
public class ReviewerGradeController {

    private final GradeService gradeService;

    public ReviewerGradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @PostMapping("/")
    public ResponseEntity<Void> submitGrade(@RequestBody EvaluationSubmissionDTO evaluationSubmission) {
        gradeService.submitGrade(evaluationSubmission);
        return ResponseEntity.ok().build();
    }

}
