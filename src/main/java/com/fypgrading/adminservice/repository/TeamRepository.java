package com.fypgrading.adminservice.repository;

import com.fypgrading.adminservice.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    @Query(
            value = "SELECT team.* " +
                    "FROM team_reviewer tr " +
                    "JOIN team ON tr.team_id = team.id " +
                    "WHERE tr.reviewer_id = :reviewerId",
            nativeQuery = true)
    List<Team> getAllReviewerTeams(UUID reviewerId);

    List<Team> findAllByOrderByIdAsc();

}
