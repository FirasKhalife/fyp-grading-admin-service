package com.fypgrading.adminservice.service;

import com.fypgrading.adminservice.entity.Reviewer;
import com.fypgrading.adminservice.entity.Team;
import com.fypgrading.adminservice.repository.ReviewerRepository;
import com.fypgrading.adminservice.service.dto.ReviewerDTO;
import com.fypgrading.adminservice.service.dto.TeamDTO;
import com.fypgrading.adminservice.service.mapper.ReviewerMapper;
import com.fypgrading.adminservice.service.mapper.TeamMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewerService {

    private final ReviewerRepository reviewerRepository;
    private final ReviewerMapper reviewerMapper;

    private final TeamMapper teamMapper;

    public ReviewerService(ReviewerRepository reviewerRepository, ReviewerMapper reviewerMapper, TeamMapper teamMapper) {
        this.reviewerRepository = reviewerRepository;
        this.reviewerMapper = reviewerMapper;
        this.teamMapper = teamMapper;
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

    public ReviewerDTO getReviewer(Integer id) {
        Reviewer reviewer = getReviewerById(id);
        return reviewerMapper.toDTO(reviewer);
    }

    public List<TeamDTO> getReviewerTeams(Integer id) {
        List<Team> teams = getReviewerById(id).getTeams();
        return teamMapper.toDTOList(teams);
    }
}
