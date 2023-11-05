package com.fypgrading.administration.controller;

import com.fypgrading.administration.service.ReviewerService;
import com.fypgrading.administration.service.dto.ReviewerDTO;
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
