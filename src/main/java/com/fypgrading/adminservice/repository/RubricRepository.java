package com.fypgrading.adminservice.repository;

import com.fypgrading.adminservice.entity.Rubric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RubricRepository extends JpaRepository<Rubric, Integer> {
}
