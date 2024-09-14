package com.fypgrading.adminservice.repository;

import com.fypgrading.adminservice.entity.TeamReviewer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.ws.rs.QueryParam;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewerTeamRepository extends JpaRepository<TeamReviewer, Long> {

    Optional<TeamReviewer> findByReviewerIdAndTeamId(Long reviewerId, Long teamId);

    List<TeamReviewer> findByReviewerId(Long reviewerId);

    @Query(
            value = "SELECT rt.* " +
                    "FROM reviewer_team AS rt " +
                        "JOIN team_reviewer_role AS trr ON trr.reviewer_id = rt.reviewer_id AND trr.team_id = :teamId " +
                        "JOIN role ON role.id = trr.role_id " +
                        "JOIN assessment ON assessment.role_id = role.id " +
                    "WHERE rt.team_id = :teamId AND assessment.id = :assessmentId",
            nativeQuery = true
    )
    List<TeamReviewer> findByTeamIdAndAssessmentId(@QueryParam("teamId") Long teamId,
                                                   @QueryParam("assessmentId") Long assessmentId);

}
