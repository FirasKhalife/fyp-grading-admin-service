package com.fypgrading.adminservice.repository;

import com.fypgrading.adminservice.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Integer> {

    @Query(
            value = "SELECT team.* " +
                    "FROM team_reviewer_role trr " +
                    "JOIN team ON trr.team_id = team.id " +
                    "WHERE trr.reviewer_id = :reviewerId",
            nativeQuery = true)
    List<Team> getAllReviewerTeams(Integer reviewerId);

}
