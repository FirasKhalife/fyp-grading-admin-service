package com.fypgrading.adminservice.repository;

import com.fypgrading.adminservice.entity.Reviewer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewerRepository extends JpaRepository<Reviewer, Integer> {

    Optional<Reviewer> findByEmail(String email);

    Boolean existsByEmail(String email);

}
