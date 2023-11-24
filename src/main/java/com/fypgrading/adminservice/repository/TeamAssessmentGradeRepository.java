package com.fypgrading.adminservice.repository;

import com.fypgrading.adminservice.entity.TeamAssessmentGrade;
import com.fypgrading.adminservice.entity.idClass.TeamAssessmentGradeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamAssessmentGradeRepository extends JpaRepository<TeamAssessmentGrade, TeamAssessmentGradeId> {

    List<TeamAssessmentGrade> findAllByTeamId(Integer teamId);

}
