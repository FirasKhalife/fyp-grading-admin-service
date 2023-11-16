package com.fypgrading.adminservice.service;

import com.fypgrading.adminservice.entity.Reviewer;
import com.fypgrading.adminservice.entity.ReviewerTeam;
import com.fypgrading.adminservice.entity.Team;
import com.fypgrading.adminservice.repository.ReviewerRepository;
import com.fypgrading.adminservice.service.dto.ReviewerDTO;
import com.fypgrading.adminservice.service.dto.ReviewerViewDTO;
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

    public List<ReviewerViewDTO> getReviewers() {
        List<Reviewer> reviewers = reviewerRepository.findAll();
        return reviewerMapper.toViewDTOList(reviewers);
    }

    public ReviewerViewDTO createReviewer(ReviewerDTO reviewerDTO) {
        Reviewer reviewer = reviewerMapper.toEntity(reviewerDTO);
        Reviewer createdEntity = reviewerRepository.save(reviewer);
        return reviewerMapper.toViewDTO(createdEntity);
    }

    public ReviewerViewDTO updateReviewer(Integer id, ReviewerDTO reviewerDTO) {
        getReviewerById(id);
        Reviewer reviewer = reviewerMapper.toEntity(reviewerDTO);
        reviewer.setId(id);
        Reviewer updatedEntity = reviewerRepository.save(reviewer);
        return reviewerMapper.toViewDTO(updatedEntity);
    }

    public ReviewerViewDTO deleteReviewer(Integer id) {
        Reviewer reviewer = getReviewerById(id);
        reviewerRepository.delete(reviewer);
        return reviewerMapper.toViewDTO(reviewer);
    }

    private Reviewer getReviewerById(Integer id) {
        return reviewerRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Reviewer not found"));
    }

    public ReviewerViewDTO getReviewer(Integer id) {
        Reviewer reviewer = getReviewerById(id);
        return reviewerMapper.toViewDTO(reviewer);
    }

    public List<TeamDTO> getReviewerTeams(Integer id) {
        List<Team> reviewerTeams =
                getReviewerById(id).getReviewerTeams().parallelStream()
                        .map(ReviewerTeam::getTeam)
                        .toList();
        return teamMapper.toDTOList(reviewerTeams);
    }
}
