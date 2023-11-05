package com.fypgrading.administration.service;

import com.fypgrading.administration.entity.Reviewer;
import com.fypgrading.administration.repository.ReviewerRepository;
import com.fypgrading.administration.service.dto.AuthDTO;
import com.fypgrading.administration.service.enums.Role;
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
