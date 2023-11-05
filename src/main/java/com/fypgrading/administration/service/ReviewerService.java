package com.fypgrading.administration.service;

import com.fypgrading.administration.entity.Reviewer;
import com.fypgrading.administration.repository.ReviewerRepository;
import com.fypgrading.administration.service.dto.ReviewerDTO;
import com.fypgrading.administration.service.mapper.ReviewerMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewerService {

    private final ReviewerRepository reviewerRepository;
    private final ReviewerMapper reviewerMapper;

    public ReviewerService(ReviewerRepository reviewerRepository, ReviewerMapper reviewerMapper) {
        this.reviewerRepository = reviewerRepository;
        this.reviewerMapper = reviewerMapper;
    }

    public List<ReviewerDTO> getReviewers() {
        List<Reviewer> reviewers = reviewerRepository.findAll();
        return reviewerMapper.toDTOList(reviewers);
    }

    public ReviewerDTO createReviewer(ReviewerDTO reviewerDTO) {
        Reviewer reviewer = reviewerMapper.toEntity(reviewerDTO);
        Reviewer createdEntity = reviewerRepository.save(reviewer);
        return reviewerMapper.toDTO(createdEntity);
    }

    public ReviewerDTO updateReviewer(Integer id, ReviewerDTO reviewerDTO) {
        getReviewerById(id);
        Reviewer reviewer = reviewerMapper.toEntity(reviewerDTO);
        reviewer.setId(id);
        Reviewer updatedEntity = reviewerRepository.save(reviewer);
        return reviewerMapper.toDTO(updatedEntity);
    }

    public ReviewerDTO deleteReviewer(Integer id) {
        Reviewer reviewer = getReviewerById(id);
        reviewerRepository.delete(reviewer);
        return reviewerMapper.toDTO(reviewer);
    }

    private Reviewer getReviewerById(Integer id) {
        return reviewerRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Reviewer not found"));
    }
}
