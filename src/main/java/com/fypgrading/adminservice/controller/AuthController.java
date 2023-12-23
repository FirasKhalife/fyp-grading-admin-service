package com.fypgrading.adminservice.controller;

import com.fypgrading.adminservice.service.ReviewerService;
import com.fypgrading.adminservice.service.dto.JwtResponseDTO;
import com.fypgrading.adminservice.service.dto.LoginDTO;
import com.fypgrading.adminservice.service.dto.ReviewerSignupDTO;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final ReviewerService reviewerService;

    public AuthController(ReviewerService reviewerService) {
        this.reviewerService = reviewerService;
        LOGGER.info("AuthController initialized");
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDTO> authenticateUser(@Valid @RequestBody LoginDTO loginDTO) {

        LOGGER.info("Attempting to authenticate user");
        JwtResponseDTO jwtResponse = reviewerService.login(loginDTO);
        LOGGER.info("User authenticated successfully");
        return ResponseEntity.ok().body(jwtResponse);
    }

    @PostMapping("/signup")
    public ResponseEntity<ReviewerSignupDTO> registerUser(@Valid @RequestBody ReviewerSignupDTO reviewerInfo) {
        LOGGER.info("Attempting to register user");
        ReviewerSignupDTO createdReviewer = reviewerService.signup(reviewerInfo);
        LOGGER.info("User registered successfully");
        return ResponseEntity.ok().body(createdReviewer);
    }

}

