package com.fypgrading.adminservice.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentScheduleRepository extends JpaRepository<AssessmentSchedule, UUID> {

    Optional<AssessmentSchedule> findByAssessment(Assessment assessment);
}
