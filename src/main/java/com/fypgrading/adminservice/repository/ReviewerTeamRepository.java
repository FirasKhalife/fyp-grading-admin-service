package com.fypgrading.adminservice.repository;

import com.fypgrading.adminservice.entity.ReviewerTeam;
import com.fypgrading.adminservice.entity.idClass.ReviewerTeamId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewerTeamRepository extends JpaRepository<ReviewerTeam, ReviewerTeamId> {

    Optional<ReviewerTeam> findByReviewerIdAndTeamId(Integer reviewerId, Integer teamId);

}
