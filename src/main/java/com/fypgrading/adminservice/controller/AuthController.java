package com.fypgrading.adminservice.controller;

import com.fypgrading.adminservice.service.ReviewerService;
import com.fypgrading.adminservice.service.dto.JwtResponseDTO;
import com.fypgrading.adminservice.service.dto.LoginDTO;
import com.fypgrading.adminservice.service.dto.ReviewerSignupDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ReviewerService reviewerService;

    public AuthController(ReviewerService reviewerService) {
        this.reviewerService = reviewerService;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDTO> authenticateUser(@Valid @RequestBody LoginDTO loginDTO) {
        JwtResponseDTO jwtResponse = reviewerService.login(loginDTO);
        return ResponseEntity.ok().body(jwtResponse);
    }

    @PostMapping("/signup")
    public ResponseEntity<ReviewerSignupDTO> registerUser(@Valid @RequestBody ReviewerSignupDTO reviewerInfo) {
        ReviewerSignupDTO createdReviewer = reviewerService.signup(reviewerInfo);
        return ResponseEntity.ok().body(createdReviewer);
    }

}
