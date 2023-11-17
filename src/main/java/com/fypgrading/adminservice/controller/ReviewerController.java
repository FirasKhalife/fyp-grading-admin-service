package com.fypgrading.adminservice.controller;

import com.fypgrading.adminservice.entity.Reviewer;
import com.fypgrading.adminservice.service.ReviewerService;
import com.fypgrading.adminservice.service.dto.ReviewerDTO;
import com.fypgrading.adminservice.service.dto.ReviewerViewDTO;
import com.fypgrading.adminservice.service.dto.TeamDTO;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/reviewers")
public class ReviewerController {

    private final ReviewerService reviewerService;

    public ReviewerController(ReviewerService reviewerService) {
        this.reviewerService = reviewerService;
    }

    @GetMapping("/self")
    public ResponseEntity<ReviewerViewDTO> getSelf() {
        Reviewer self =
                (Reviewer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ReviewerViewDTO reviewer = reviewerService.getReviewer(self.getId());
        return ResponseEntity.ok().body(reviewer);
    }

    @RolesAllowed("ADMIN")
    @GetMapping("/")
    public ResponseEntity<List<ReviewerViewDTO>> getReviewers() {
        List<ReviewerViewDTO> rubrics = reviewerService.getReviewers();
        return ResponseEntity.ok().body(rubrics);
    }

    @RolesAllowed("ADMIN")
    @PostMapping("/")
    public ResponseEntity<ReviewerViewDTO> createReviewer(@RequestBody ReviewerDTO rubricDTO) {
        ReviewerViewDTO createdReviewer = reviewerService.createReviewer(rubricDTO);
        return ResponseEntity.ok().body(createdReviewer);
    }

    @RolesAllowed("ADMIN")
    @GetMapping("/{id: [0-9]+}")
    public ResponseEntity<ReviewerViewDTO> getReviewer(@PathVariable Integer id) {
        ReviewerViewDTO rubrics = reviewerService.getReviewer(id);
        return ResponseEntity.ok().body(rubrics);
    }

    @RolesAllowed("ADMIN")
    @GetMapping("/{id}/teams")
    public ResponseEntity<List<TeamDTO>> getReviewerTeams(@PathVariable Integer id) {
        List<TeamDTO> teams = reviewerService.getReviewerTeams(id);
        return ResponseEntity.ok().body(teams);
    }

    @RolesAllowed("ADMIN")
    @DeleteMapping("/{id}")
    public ResponseEntity<ReviewerViewDTO> deleteReviewer(@PathVariable Integer id) {
        ReviewerViewDTO rubrics = reviewerService.deleteReviewer(id);
        return ResponseEntity.ok().body(rubrics);
    }
    
}
