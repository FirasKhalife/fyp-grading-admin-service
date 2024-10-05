package com.fypgrading.adminservice.repository;

import com.fypgrading.adminservice.entity.SystemRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemRoleRepository extends JpaRepository<SystemRole, Long> {

    List<SystemRole> findAllByNameIn(List<String> names);

}
