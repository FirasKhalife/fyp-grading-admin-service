package com.fypgrading.adminservice.controller;

import com.fypgrading.adminservice.service.ReviewerTeamService;
import com.fypgrading.adminservice.service.dto.JwtResponseDTO;
import com.fypgrading.adminservice.service.dto.LoginDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ReviewerTeamService reviewerTeamService;

    public AuthController(ReviewerTeamService reviewerTeamService) {
        this.reviewerTeamService = reviewerTeamService;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDTO> authenticateUser(@Valid @RequestBody LoginDTO auth) {
        JwtResponseDTO response =
                auth.getPassword().equals("admin") ?
                        JwtResponseDTO.builder()
                                .id(1).email("rima.kilany@net.usj.edu.lb")
                                .firstName("Rima").lastName("Kilany")
                                .accessToken("accessToken").isAdmin(true)
                                .build()
                        :
                        auth.getPassword().equals("fifo") ?
                                JwtResponseDTO.builder()
                                        .id(2).email("firas.khalife@net.usj.edu.lb")
                                        .firstName("Firas").lastName("Khalife")
                                        .accessToken("accessToken")
                                        .build()
                                :
                                JwtResponseDTO.builder()
                                        .id(3).email("gaelle.said@net.usj.edu.lb")
                                        .firstName("Gaelle").lastName("Said")
                                        .accessToken("accessToken")
                                        .build();

        response.setRoles(reviewerTeamService.getReviewerRoles(response.getId()).getRoles());

        return ResponseEntity.ok().body(response);
    }

//    @PostMapping("/signup")
//    public ResponseEntity<ReviewerViewDTO> registerUser(@Valid @RequestBody ReviewerDTO reviewerDTO) {
//        ReviewerViewDTO reviewer = authService.signup(reviewerDTO);
//        return ResponseEntity.ok().body(reviewer);
//    }

}
