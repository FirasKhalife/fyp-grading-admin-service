package com.fypgrading.adminservice.controller;

import com.fypgrading.adminservice.entity.Reviewer;
import com.fypgrading.adminservice.service.AuthService;
import com.fypgrading.adminservice.service.dto.AuthDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Reviewer> login(@RequestBody AuthDTO authDTO) {
        Reviewer role = authService.login(authDTO);
        return ResponseEntity.ok().body(role);
    }
}
