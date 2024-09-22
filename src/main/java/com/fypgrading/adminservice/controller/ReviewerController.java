package com.fypgrading.adminservice.controller;

import com.fypgrading.adminservice.service.ReviewerService;
import com.fypgrading.adminservice.service.TeamReviewerService;
import com.fypgrading.adminservice.service.dto.*;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/reviewers")
public class ReviewerController {

    private final ReviewerService reviewerService;
    private final TeamReviewerService teamReviewerService;

    @GetMapping("/{id}/home")
    public ResponseEntity<ReviewerHomeDTO> getReviewerHome(@PathVariable Long id) {
        ReviewerHomeDTO reviewerHome = reviewerService.getReviewerHome(id);
        return ResponseEntity.ok().body(reviewerHome);
    }

    @GetMapping("/{id}/teams/{teamId}/roles")
    public ResponseEntity<TeamReviewerRolesDTO> getTeamReviewerRoles(@PathVariable Long id,
                                                                     @PathVariable Long teamId) {
        TeamReviewerRolesDTO roles = teamReviewerService.getTeamReviewerRoles(id, teamId);
        return ResponseEntity.ok().body(roles);
    }

    @GetMapping("/{id}/roles")
    public ResponseEntity<ReviewerRolesDTO> getReviewerRoles(@PathVariable Long id) {
        ReviewerRolesDTO roles = teamReviewerService.getReviewerRoles(id);
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
