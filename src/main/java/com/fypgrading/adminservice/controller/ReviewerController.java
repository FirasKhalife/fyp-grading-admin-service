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

    @GetMapping("/{id}/home")
    public ResponseEntity<ReviewerHomeDTO> getReviewerHome(@PathVariable Integer id) {
        ReviewerHomeDTO reviewerHome = reviewerService.getReviewerHome(id);
        return ResponseEntity.ok().body(reviewerHome);
    }

    @GetMapping("/{id}/teams/{teamId}/roles")
    public ResponseEntity<TeamReviewerRolesDTO> getReviewerTeamRoles(@PathVariable Integer id,
                                                                     @PathVariable Integer teamId) {
        TeamReviewerRolesDTO roles = reviewerTeamService.getReviewerTeamRoles(id, teamId);
        return ResponseEntity.ok().body(roles);
    }

    @GetMapping("/{id}/roles")
    public ResponseEntity<ReviewerRolesDTO> getReviewerRoles(@PathVariable Integer id) {
        ReviewerRolesDTO roles = reviewerTeamService.getReviewerRoles(id);
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

    @GetMapping("/{id}")
    public ResponseEntity<ReviewerDTO> getReviewer(@PathVariable Integer id) {
        ReviewerDTO rubrics = reviewerService.getReviewerViewById(id);
        return ResponseEntity.ok().body(rubrics);
    }

    @GetMapping("/{id}/teams")
    public ResponseEntity<ReviewerTeamsAssessmentsDTO> getReviewerTeams(@PathVariable Integer id) {
        ReviewerTeamsAssessmentsDTO teams = reviewerService.getReviewerTeamsAssessments(id);
        return ResponseEntity.ok().body(teams);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ReviewerDTO> deleteReviewer(@PathVariable Integer id) {
        ReviewerDTO rubrics = reviewerService.deleteReviewer(id);
        return ResponseEntity.ok().body(rubrics);
    }
    
}
