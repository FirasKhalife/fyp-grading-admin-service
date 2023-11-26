package com.fypgrading.adminservice.controller;

import com.fypgrading.adminservice.service.ReviewerService;
import com.fypgrading.adminservice.service.ReviewerTeamService;
import com.fypgrading.adminservice.service.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/reviewers")
public class ReviewerController {

    private final ReviewerService reviewerService;
    private final ReviewerTeamService reviewerTeamService;

    public ReviewerController(ReviewerService reviewerService, ReviewerTeamService reviewerTeamService) {
        this.reviewerTeamService = reviewerTeamService;
        this.reviewerService = reviewerService;
    }

    @GetMapping("/{reviewerId}/home")
    public ResponseEntity<ReviewerHomeDTO> getReviewerHome(@PathVariable Integer reviewerId) {
        ReviewerHomeDTO reviewerHome = reviewerService.getReviewerHome(reviewerId);
        return ResponseEntity.ok().body(reviewerHome);
    }

    @GetMapping("/{reviewerId}/teams/{teamId}/roles")
    public ResponseEntity<TeamReviewerRolesDTO> getReviewerTeamRoles(@PathVariable Integer reviewerId,
                                                                     @PathVariable Integer teamId) {
        TeamReviewerRolesDTO roles = reviewerTeamService.getReviewerTeamRoles(reviewerId, teamId);
        return ResponseEntity.ok().body(roles);
    }

    @GetMapping("/{reviewerId}/roles")
    public ResponseEntity<ReviewerRolesDTO> getReviewerRoles(@PathVariable Integer reviewerId) {
        ReviewerRolesDTO roles = reviewerTeamService.getReviewerRoles(reviewerId);
        return ResponseEntity.ok().body(roles);
    }

    @GetMapping("/")
    public ResponseEntity<List<ReviewerDTO>> getReviewers() {
        List<ReviewerDTO> rubrics = reviewerService.getReviewers();
        return ResponseEntity.ok().body(rubrics);
    }

    @PostMapping("/")
    public ResponseEntity<ReviewerDTO> createReviewer(@RequestBody ReviewerLoginDTO rubricDTO) {
        ReviewerDTO createdReviewer = reviewerService.createReviewer(rubricDTO);
        return ResponseEntity.ok().body(createdReviewer);
    }

    @GetMapping("/{reviewerId: [0-9]+}")
    public ResponseEntity<ReviewerDTO> getReviewer(@PathVariable Integer reviewerId) {
        ReviewerDTO rubrics = reviewerService.getReviewerViewById(reviewerId);
        return ResponseEntity.ok().body(rubrics);
    }

    @GetMapping("/{reviewerId}/teams")
    public ResponseEntity<ReviewerTeamsAssessmentsDTO> getReviewerTeams(@PathVariable Integer reviewerId) {
        ReviewerTeamsAssessmentsDTO teams = reviewerService.getReviewerTeamsAssessments(reviewerId);
        return ResponseEntity.ok().body(teams);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ReviewerDTO> deleteReviewer(@PathVariable Integer id) {
        ReviewerDTO rubrics = reviewerService.deleteReviewer(id);
        return ResponseEntity.ok().body(rubrics);
    }
    
}
