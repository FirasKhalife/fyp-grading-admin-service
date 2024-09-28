package com.fypgrading.adminservice.repository;

import com.fypgrading.adminservice.entity.TeamReviewer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewerTeamRepository extends JpaRepository<TeamReviewer, Long> {

    Optional<TeamReviewer> findByReviewerIdAndTeamId(UUID reviewerId, Long teamId);

    List<TeamReviewer> findByReviewerId(UUID reviewerId);

    @Query(
            value = "SELECT tr.* " +
                    "FROM team_reviewer AS tr " +
                        "JOIN team_reviewer_role AS trr ON trr.team_reviewer_id = tr.id " +
                        "JOIN role ON role.id = trr.role_id " +
                        "JOIN assessment ON assessment.role_id = role.id " +
                    "WHERE tr.team_id = :teamId AND assessment.id = :assessmentId",
            nativeQuery = true
    )
    List<TeamReviewer> findByTeamIdAndAssessmentId(@Param("teamId") Long teamId,
                                                   @Param("assessmentId") Long assessmentId);

}
