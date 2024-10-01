package com.fypgrading.adminservice.repository;

import com.fypgrading.adminservice.entity.Reviewer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewerRepository extends JpaRepository<Reviewer, UUID> {

    @Query(
        value =
            "SELECT reviewer.* " +
            "FROM team_reviewer tr " +
                "JOIN reviewer ON tr.reviewer_id = reviewer.id " +
            "WHERE tr.team_id = :teamId",
        nativeQuery = true
    )
    List<Reviewer> findAllTeamReviewers(Long teamId);

    @Query(
        value =
            "SELECT COUNT(tr.reviewer_id) " +
            "FROM team_reviewer tr " +
                "JOIN team_reviewer_role trr ON tr.id = trr.team_reviewer_id " +
                "JOIN reviewer_role ON trr.reviewer_role_id = reviewer_role.id " +
                "JOIN assessment ON assessment.reviewer_role_id = reviewer_role.id " +
            "WHERE tr.team_id = :teamId AND assessment.id = :assessmentId",
        nativeQuery = true
    )
    long countReviewersByTeamIdAndAssessmentId(@Param("teamId") Long teamId, @Param("assessmentId") Long assessmentId);

}
