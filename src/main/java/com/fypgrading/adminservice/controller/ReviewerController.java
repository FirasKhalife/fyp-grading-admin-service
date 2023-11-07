package com.fypgrading.adminservice.controller;

import com.fypgrading.adminservice.service.ReviewerService;
import com.fypgrading.adminservice.service.dto.ReviewerDTO;
import com.fypgrading.adminservice.service.dto.TeamDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviewers")
public class ReviewerController {

    private final ReviewerService reviewerService;

    public ReviewerController(ReviewerService reviewerService) {
        this.reviewerService = reviewerService;
    }

    @GetMapping("/")
    public ResponseEntity<List<ReviewerDTO>> getReviewers() {
        List<ReviewerDTO> rubrics = reviewerService.getReviewers();
        return ResponseEntity.ok().body(rubrics);
    }

    @PostMapping("/")
    public ResponseEntity<ReviewerDTO> createReviewer(@RequestBody ReviewerDTO rubricDTO) {
        ReviewerDTO createdReviewer = reviewerService.createReviewer(rubricDTO);
        return ResponseEntity.ok().body(createdReviewer);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewerDTO> getReviewer(@PathVariable Integer id) {
        ReviewerDTO rubrics = reviewerService.getReviewer(id);
        return ResponseEntity.ok().body(rubrics);
    }

    @GetMapping("/{id}/teams")
    public ResponseEntity<List<TeamDTO>> getReviewerTeams(@PathVariable Integer id) {
        List<TeamDTO> teams = reviewerService.getReviewerTeams(id);
        return ResponseEntity.ok().body(teams);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewerDTO> updateReviewer(@PathVariable Integer id, @RequestBody ReviewerDTO rubricDTO) {
        ReviewerDTO rubrics = reviewerService.updateReviewer(id, rubricDTO);
        return ResponseEntity.ok().body(rubrics);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ReviewerDTO> deleteReviewer(@PathVariable Integer id) {
        ReviewerDTO rubrics = reviewerService.deleteReviewer(id);
        return ResponseEntity.ok().body(rubrics);
    }
    
}
