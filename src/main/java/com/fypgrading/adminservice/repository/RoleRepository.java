package com.fypgrading.adminservice.repository;

import com.fypgrading.adminservice.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    @Query(
            value = "SELECT DISTINCT role.* " +
                    "FROM team_reviewer_role trr " +
                    "JOIN role ON trr.role_id = role.id " +
                    "WHERE trr.reviewer_id = :reviewerId",
            nativeQuery = true)
    List<Role> getDistinctReviewerRoles(Integer reviewerId);

    @Query(
            value = "SELECT role.* " +
                    "FROM team_reviewer_role trr " +
                    "JOIN role ON trr.role_id = role.id " +
                    "WHERE trr.reviewer_id = :reviewerId AND trr.team_id = :teamId",
            nativeQuery = true)
    List<Role> getReviewerTeamRoles(Integer reviewerId, Integer teamId);
}
