package com.fypgrading.adminservice.controller;

import com.fypgrading.adminservice.service.dto.JwtResponse;
import com.fypgrading.adminservice.service.dto.LoginDTO;
import com.fypgrading.adminservice.service.enums.RoleEnum;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginDTO auth) {
        JwtResponse response =
                new JwtResponse(1, "firas.khalife@net.usj.edu.lb",
                        "Rima", "Kilany", "accessToken", RoleEnum.ADMIN);
        return ResponseEntity.ok().body(response);
    }

//    @PostMapping("/signup")
//    public ResponseEntity<ReviewerViewDTO> registerUser(@Valid @RequestBody ReviewerDTO reviewerDTO) {
//        ReviewerViewDTO reviewer = authService.signup(reviewerDTO);
//        return ResponseEntity.ok().body(reviewer);
//    }

}
