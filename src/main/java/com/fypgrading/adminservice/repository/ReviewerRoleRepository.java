package com.fypgrading.adminservice.repository;

import com.fypgrading.adminservice.entity.ReviewerRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewerRoleRepository extends JpaRepository<ReviewerRole, Long> {

    @Query(
        value =
            "SELECT trr.* " +
            "FROM team_reviewer_role trr " +
                "JOIN team_reviewer tr ON tr.id = trr.team_reviewer_id " +
                "JOIN reviewer_role rr ON trr.reviewer_role_id = rr.id " +
            "WHERE tr.reviewer_id = :reviewerId",
        nativeQuery = true
    )
    List<ReviewerRole> getReviewerRolesByReviewerId(UUID reviewerId);

    @Query(
        value =
            "SELECT reviewer_role.* " +
            "FROM team_reviewer tr " +
                "JOIN team_reviewer_role trr ON tr.id = trr.team_reviewer_id " +
                "JOIN reviewer_role ON trr.reviewer_role_id = reviewer_role.id " +
            "WHERE tr.reviewer_id = :reviewerId AND tr.team_id = :teamId",
        nativeQuery = true
    )
    List<ReviewerRole> getReviewerTeamRoles(UUID reviewerId, Long teamId);
}
