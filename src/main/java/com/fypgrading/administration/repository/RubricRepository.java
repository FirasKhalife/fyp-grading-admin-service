package com.fypgrading.administration.repository;

import com.fypgrading.administration.entity.Rubric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RubricRepository extends JpaRepository<Rubric, Integer> {
}
