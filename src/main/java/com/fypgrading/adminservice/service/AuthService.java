package com.fypgrading.adminservice.service;

import com.fypgrading.adminservice.config.security.jwt.JwtUtils;
import com.fypgrading.adminservice.entity.Reviewer;
import com.fypgrading.adminservice.repository.ReviewerRepository;
import com.fypgrading.adminservice.service.dto.JwtResponse;
import com.fypgrading.adminservice.service.dto.LoginDTO;
import com.fypgrading.adminservice.service.dto.ReviewerDTO;
import com.fypgrading.adminservice.service.dto.ReviewerViewDTO;
import com.fypgrading.adminservice.service.mapper.ReviewerMapper;
import jakarta.persistence.EntityExistsException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final ReviewerRepository reviewerRepository;
    private final ReviewerMapper reviewerMapper;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    public AuthService(AuthenticationManager authenticationManager,
                       ReviewerRepository reviewerRepository,
                       ReviewerMapper reviewerMapper,
                       PasswordEncoder encoder,
                       JwtUtils jwtUtils
    ) {
        this.authenticationManager = authenticationManager;
        this.reviewerRepository = reviewerRepository;
        this.reviewerMapper = reviewerMapper;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
    }

    public JwtResponse login(LoginDTO auth) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(auth.getEmail(), auth.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        Reviewer userDetails = (Reviewer) authentication.getPrincipal();

        return
                new JwtResponse(
                        userDetails.getId(),
                        userDetails.getEmail(),
                        userDetails.getFirstName(),
                        userDetails.getLastName(),
                        jwt,
                        userDetails.getRole()
                );
    }

    public ReviewerViewDTO signup(ReviewerDTO reviewerDTO) {
        if (reviewerRepository.existsByEmail(reviewerDTO.getEmail())) {
            throw new EntityExistsException("Email already registered");
        }

        Reviewer reviewer = new Reviewer(
                reviewerDTO.getFirstName(),
                reviewerDTO.getLastName(),
                reviewerDTO.getEmail(),
                encoder.encode(reviewerDTO.getPassword()),
                reviewerDTO.getRole()
        );
        Reviewer savedReviewer = reviewerRepository.save(reviewer);

        return reviewerMapper.toViewDTO(savedReviewer);
    }
}
