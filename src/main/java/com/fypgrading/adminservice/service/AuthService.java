package com.fypgrading.adminservice.service;

import com.fypgrading.adminservice.entity.Reviewer;
import com.fypgrading.adminservice.repository.ReviewerRepository;
import com.fypgrading.adminservice.service.dto.AuthDTO;
import com.fypgrading.adminservice.service.dto.ReviewerDTO;
import com.fypgrading.adminservice.service.mapper.ReviewerMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final ReviewerRepository reviewerRepository;
    private final ReviewerMapper reviewerMapper;

    public AuthService(ReviewerRepository reviewerRepository, ReviewerMapper reviewerMapper) {
        this.reviewerRepository = reviewerRepository;
        this.reviewerMapper = reviewerMapper;
    }

    public ReviewerDTO login(AuthDTO authDTO) {
        Reviewer reviewer =
                reviewerRepository.findByEmailAndPassword(authDTO.getEmail(), authDTO.getPassword())
                        .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return reviewerMapper.toDTO(reviewer);
    }
}
