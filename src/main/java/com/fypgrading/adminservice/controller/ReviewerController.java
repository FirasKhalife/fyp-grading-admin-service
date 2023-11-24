package com.fypgrading.adminservice.controller;

import com.fypgrading.adminservice.service.ReviewerService;
import com.fypgrading.adminservice.service.ReviewerTeamService;
import com.fypgrading.adminservice.service.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/reviewers")
public class ReviewerController {

    private final ReviewerService reviewerService;
    private final ReviewerTeamService reviewerTeamService;

    public ReviewerController(ReviewerService reviewerService, ReviewerTeamService reviewerTeamService) {
        this.reviewerTeamService = reviewerTeamService;
        this.reviewerService = reviewerService;
    }

    @GetMapping("/{reviewerId}/teams/{teamId}/roles")
    public ResponseEntity<ReviewerTeamRolesDTO> getReviewerTeamRoles(@PathVariable Integer reviewerId,
                                                                     @PathVariable Integer teamId) {
        ReviewerTeamRolesDTO roles = reviewerTeamService.getReviewerTeamRoles(reviewerId, teamId);
        return ResponseEntity.ok().body(roles);
    }

    @GetMapping("/{reviewerId}/roles")
    public ResponseEntity<ReviewerRolesDTO> getReviewerRoles(@PathVariable Integer reviewerId) {
        ReviewerRolesDTO roles = reviewerTeamService.getReviewerRoles(reviewerId);
        return ResponseEntity.ok().body(roles);
    }

    @GetMapping("/")
    public ResponseEntity<List<ReviewerViewDTO>> getReviewers() {
        List<ReviewerViewDTO> rubrics = reviewerService.getReviewers();
        return ResponseEntity.ok().body(rubrics);
    }

    @PostMapping("/")
    public ResponseEntity<ReviewerViewDTO> createReviewer(@RequestBody ReviewerDTO rubricDTO) {
        ReviewerViewDTO createdReviewer = reviewerService.createReviewer(rubricDTO);
        return ResponseEntity.ok().body(createdReviewer);
    }

    @GetMapping("/{id: [0-9]+}")
    public ResponseEntity<ReviewerViewDTO> getReviewer(@PathVariable Integer id) {
        ReviewerViewDTO rubrics = reviewerService.getReviewerViewById(id);
        return ResponseEntity.ok().body(rubrics);
    }

    @GetMapping("/{id}/teams")
    public ResponseEntity<List<TeamDTO>> getReviewerTeams(@PathVariable Integer id) {
        List<TeamDTO> teams = reviewerService.getReviewerTeams(id);
        return ResponseEntity.ok().body(teams);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ReviewerViewDTO> deleteReviewer(@PathVariable Integer id) {
        ReviewerViewDTO rubrics = reviewerService.deleteReviewer(id);
        return ResponseEntity.ok().body(rubrics);
    }
    
}
