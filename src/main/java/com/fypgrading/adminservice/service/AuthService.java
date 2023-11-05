package com.fypgrading.adminservice.service;

import com.fypgrading.adminservice.entity.Reviewer;
import com.fypgrading.adminservice.repository.ReviewerRepository;
import com.fypgrading.adminservice.service.dto.AuthDTO;
import com.fypgrading.adminservice.service.enums.Role;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final ReviewerRepository reviewerRepository;

    public AuthService(ReviewerRepository reviewerRepository) {
        this.reviewerRepository = reviewerRepository;
    }

    public Role login(AuthDTO authDTO) {
        return reviewerRepository.findByEmailAndPassword(authDTO.getEmail(), authDTO.getPassword())
                .map(Reviewer::getRole)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}
