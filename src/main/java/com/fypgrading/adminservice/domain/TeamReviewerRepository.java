package com.fypgrading.adminservice.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamReviewerRepository extends JpaRepository<TeamReviewer, UUID> {

    List<TeamReviewer> findByTeamId(UUID teamId);

    List<TeamReviewer> findByReviewerId(UUID reviewerId);

    Optional<TeamReviewer> findByTeamIdAndReviewerId(UUID teamId, UUID reviewerId);

    long countByReviewerId(UUID reviewerId);

    void deleteByTeamId(UUID teamId);
}
