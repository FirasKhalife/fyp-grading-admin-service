package com.fypgrading.adminservice.service;

import com.fypgrading.adminservice.entity.SystemRole;
import com.fypgrading.adminservice.repository.SystemRoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class SystemRoleService {

    private final SystemRoleRepository systemRoleRepository;

    public List<SystemRole> findAllByNames(List<String> names) {
        return systemRoleRepository.findAllByNameIn(names);
    }

}
