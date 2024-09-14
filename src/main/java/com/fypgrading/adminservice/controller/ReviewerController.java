package com.fypgrading.adminservice.controller;

import com.fypgrading.adminservice.service.ReviewerService;
import com.fypgrading.adminservice.service.TeamReviewerService;
import com.fypgrading.adminservice.service.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

//@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/reviewers")
public class ReviewerController {

    Logger logger = Logger.getLogger(ReviewerController.class.getName());

    private final ReviewerService reviewerService;
    private final TeamReviewerService teamReviewerService;

    public ReviewerController(ReviewerService reviewerService, TeamReviewerService teamReviewerService) {
        this.teamReviewerService = teamReviewerService;
        this.reviewerService = reviewerService;
        logger.info("ReviewerController initialized");
    }

    @GetMapping("/{id}/home")
    public ResponseEntity<ReviewerHomeDTO> getReviewerHome(@PathVariable Long id) {
        logger.info("Getting reviewer home for reviewerId");
        ReviewerHomeDTO reviewerHome = reviewerService.getReviewerHome(id);
        logger.info("Found reviewer home for reviewerId");
        return ResponseEntity.ok().body(reviewerHome);
    }

    @GetMapping("/{id}/teams/{teamId}/roles")
    public ResponseEntity<TeamReviewerRolesDTO> getTeamReviewerRoles(@PathVariable Long id,
                                                                     @PathVariable Long teamId) {
        logger.info("Getting reviewer team roles for reviewerId and teamId");
        TeamReviewerRolesDTO roles = teamReviewerService.getTeamReviewerRoles(id, teamId);
        logger.info("Found reviewer team roles for reviewerId and teamId");
        return ResponseEntity.ok().body(roles);
    }

    @GetMapping("/{id}/roles")
    public ResponseEntity<ReviewerRolesDTO> getReviewerRoles(@PathVariable Long id) {
        logger.info("Getting reviewer roles for reviewerId");
        ReviewerRolesDTO roles = teamReviewerService.getReviewerRoles(id);
        logger.info("Found reviewer roles for reviewerId");
        return ResponseEntity.ok().body(roles);
    }

    @GetMapping("/")
    public ResponseEntity<List<ReviewerDTO>> getReviewers() {
        logger.info("Getting all reviewers");
        List<ReviewerDTO> rubrics = reviewerService.getReviewers();
        logger.info("Found reviewers");
        return ResponseEntity.ok().body(rubrics);
    }

    @PostMapping("/")
    public ResponseEntity<ReviewerDTO> createReviewer(@RequestBody ReviewerLoginDTO rubricDTO) {
        logger.info("Creating reviewer");
        ReviewerDTO createdReviewer = reviewerService.createReviewer(rubricDTO);
        logger.info("Reviewer created");
        return ResponseEntity.ok().body(createdReviewer);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewerDTO> getReviewer(@PathVariable Long id) {
        ReviewerDTO rubrics = reviewerService.getReviewerViewById(id);
        return ResponseEntity.ok().body(rubrics);
    }

    @GetMapping("/{id}/teams")
    public ResponseEntity<ReviewerTeamsAssessmentsDTO> getReviewerTeams(@PathVariable Long id) {
        ReviewerTeamsAssessmentsDTO teams = reviewerService.getReviewerTeamsAssessments(id);
        return ResponseEntity.ok().body(teams);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ReviewerDTO> deleteReviewer(@PathVariable Long id) {
        ReviewerDTO rubrics = reviewerService.deleteReviewer(id);
        return ResponseEntity.ok().body(rubrics);
    }
    
}
