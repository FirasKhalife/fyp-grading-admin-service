package com.fypgrading.adminservice.repository;

import com.fypgrading.adminservice.entity.Assessment;
import com.fypgrading.adminservice.entity.ReviewerRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    List<Assessment> findAllByReviewerRoleInOrderById(List<ReviewerRole> roles);

}
