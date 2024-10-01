package com.fypgrading.adminservice.controller;

import com.fypgrading.adminservice.service.ReviewerService;
import com.fypgrading.adminservice.service.TeamReviewerService;
import com.fypgrading.adminservice.service.dto.*;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/api/reviewers")
public class ReviewerController {

    private final TeamReviewerService teamReviewerService;
    private final ReviewerService reviewerService;

    @GetMapping("/home")
    public ResponseEntity<ReviewerHomeDTO> getReviewerHome() {
        ReviewerHomeDTO reviewerHome = reviewerService.getReviewerHome();
        return ResponseEntity.ok().body(reviewerHome);
    }

    @GetMapping("/{id}/teams/{teamId}/roles")
    public ResponseEntity<TeamReviewerRolesDTO> getTeamReviewerRoles(@PathVariable UUID id,
                                                                     @PathVariable Long teamId) {
        TeamReviewerRolesDTO roles = teamReviewerService.getTeamReviewerRoles(id, teamId);
        return ResponseEntity.ok().body(roles);
    }

    @GetMapping("/{id}/roles")
    public ResponseEntity<ReviewerRolesDTO> getReviewerRoles(@PathVariable UUID id) {
        ReviewerRolesDTO roles = teamReviewerService.getReviewerRoles(id);
        return ResponseEntity.ok().body(roles);
    }

    @GetMapping("/")
    public ResponseEntity<List<ReviewerDTO>> getAllReviewers() {
        List<ReviewerDTO> rubrics = reviewerService.getAllReviewers();
        return ResponseEntity.ok().body(rubrics);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewerDTO> getReviewerView(@PathVariable UUID id) {
        ReviewerDTO rubrics = reviewerService.getReviewerViewById(id);
        return ResponseEntity.ok().body(rubrics);
    }

    @GetMapping("/{id}/teams")
    public ResponseEntity<ReviewerTeamsAssessmentsDTO> getReviewerTeams(@PathVariable UUID id) {
        ReviewerTeamsAssessmentsDTO teams = reviewerService.getReviewerTeamsAssessments(id);
        return ResponseEntity.ok().body(teams);
    }
    
}
