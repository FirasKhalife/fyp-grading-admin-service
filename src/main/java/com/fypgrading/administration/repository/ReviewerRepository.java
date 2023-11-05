package com.fypgrading.administration.repository;

import com.fypgrading.administration.entity.Reviewer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewerRepository extends JpaRepository<Reviewer, Integer> {

    Optional<Reviewer> findByEmailAndPassword(String email, String password);
}
