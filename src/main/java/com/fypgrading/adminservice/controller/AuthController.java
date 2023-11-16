package com.fypgrading.adminservice.controller;

import com.fypgrading.adminservice.service.AuthService;
import com.fypgrading.adminservice.service.dto.JwtResponse;
import com.fypgrading.adminservice.service.dto.LoginDTO;
import com.fypgrading.adminservice.service.dto.ReviewerDTO;
import com.fypgrading.adminservice.service.dto.ReviewerViewDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginDTO auth) {
        JwtResponse response = authService.login(auth);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<ReviewerViewDTO> registerUser(@Valid @RequestBody ReviewerDTO reviewerDTO) {
        ReviewerViewDTO reviewer = authService.signup(reviewerDTO);
        return ResponseEntity.ok().body(reviewer);
    }

}
