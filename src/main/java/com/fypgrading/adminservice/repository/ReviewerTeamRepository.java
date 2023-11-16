package com.fypgrading.adminservice.repository;

import com.fypgrading.adminservice.entity.ReviewerTeam;
import com.fypgrading.adminservice.entity.ReviewerTeamKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewerTeamRepository extends JpaRepository<ReviewerTeam, ReviewerTeamKey> {

    Long countByTeamId(int teamId);
}
