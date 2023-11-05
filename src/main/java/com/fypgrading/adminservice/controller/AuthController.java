package com.fypgrading.adminservice.controller;

import com.fypgrading.adminservice.service.AuthService;
import com.fypgrading.adminservice.service.dto.AuthDTO;
import com.fypgrading.adminservice.service.enums.Role;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/login")
    public ResponseEntity<Role> login(@RequestBody AuthDTO authDTO) {
        Role role = authService.login(authDTO);
        return ResponseEntity.ok().body(role);
    }
}
