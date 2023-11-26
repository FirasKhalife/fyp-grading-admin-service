package com.fypgrading.adminservice.repository;

import com.fypgrading.adminservice.entity.Reviewer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.ws.rs.QueryParam;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewerRepository extends JpaRepository<Reviewer, Integer> {

    Optional<Reviewer> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query(
            value = "SELECT reviewer.* " +
                    "FROM team_reviewer_role trr " +
                        "JOIN reviewer ON trr.reviewer_id = reviewer.id " +
                    "WHERE trr.team_id = :teamId",
            nativeQuery = true
    )
    List<Reviewer> findAllTeamReviewers(Integer teamId);

    @Query(
            value = "SELECT COUNT(trr.reviewer_id) " +
                    "FROM team_reviewer_role AS trr " +
                        "JOIN role ON trr.role_id = role.id " +
                        "JOIN assessment ON assessment.role_id = role.id " +
                    "WHERE trr.team_id = :teamId AND assessment.id = :assessmentId",
            nativeQuery = true
    )
    long countReviewersByTeamIdAndAssessmentId(@QueryParam("teamId") Integer teamId,
                                               @QueryParam("assessmentId") Integer assessmentId);

}
