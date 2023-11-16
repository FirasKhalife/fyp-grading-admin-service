package com.fypgrading.adminservice.config.security;

import com.fypgrading.adminservice.entity.Reviewer;
import com.fypgrading.adminservice.repository.ReviewerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final ReviewerRepository reviewerRepository;

    public CustomUserDetailsService(ReviewerRepository reviewerRepository) {
        this.reviewerRepository = reviewerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return reviewerRepository.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException(email));
    }
}