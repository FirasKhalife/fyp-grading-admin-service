package com.fypgrading.adminservice.repository;

import com.fypgrading.adminservice.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    @Query(
        value = "SELECT DISTINCT role.* " +
                    "FROM team_reviewer_role trr " +
                        "JOIN team_reviewer tr ON tr.id = trr.team_reviewer_id " +
                        "JOIN role ON trr.role_id = role.id " +
                    "WHERE tr.reviewer_id = :reviewerId",
        nativeQuery = true
    )
    List<Role> getDistinctReviewerRoles(Long reviewerId);

    @Query(
            value = "SELECT role.* " +
                    "FROM team_reviewer tr " +
                        "JOIN team_reviewer_role trr ON tr.id = trr.team_reviewer_id " +
                        "JOIN role ON trr.role_id = role.id " +
                    "WHERE tr.reviewer_id = :reviewerId AND tr.team_id = :teamId",
            nativeQuery = true
    )
    List<Role> getReviewerTeamRoles(Long reviewerId, Long teamId);
}
