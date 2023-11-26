package com.fypgrading.adminservice.repository;

import com.fypgrading.adminservice.entity.Assessment;
import com.fypgrading.adminservice.entity.Team;
import com.fypgrading.adminservice.entity.TeamAssessment;
import com.fypgrading.adminservice.entity.idClass.TeamAssessmentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.ws.rs.QueryParam;
import java.util.List;

@Repository
public interface TeamAssessmentRepository extends JpaRepository<TeamAssessment, TeamAssessmentId> {

    List<TeamAssessment> findAllByTeamId(Integer teamId);

    List<TeamAssessment> findAllByTeamAndAssessmentIsNot(Team team, Assessment assessment);

    @Query("SELECT t.grade " +
            "FROM TeamAssessment t " +
            "WHERE t.team.id = :teamId " +
            "ORDER BY t.assessment.id ASC"
    )
    List<Float> findAllGradesByTeamIdOrderByAssessmentId(@QueryParam("teamId") Integer teamId);

}
