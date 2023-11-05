package com.fypgrading.adminservice.config;

import com.fypgrading.adminservice.entity.Reviewer;
import com.fypgrading.adminservice.repository.ReviewerRepository;
import com.fypgrading.adminservice.service.enums.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;
import java.util.Optional;

public class AuthenticationInterceptor implements HandlerInterceptor {

    private final ReviewerRepository reviewerRepository;

    public AuthenticationInterceptor(ReviewerRepository reviewerRepository) {
        this.reviewerRepository = reviewerRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        if (request.getRequestURI().equals("/api/auth/login"))
            return true;

        String username = request.getHeader("email");
        String password = request.getHeader("password");

        Optional<Reviewer> reviewerOpt = reviewerRepository.findByEmailAndPassword(username, password);
        if (reviewerOpt.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized");
            return false; // Stop further processing of the request
        }

        Reviewer reviewer = reviewerOpt.get();
        if (!Objects.equals(request.getMethod(), "GET") && reviewer.getRole() != Role.ADMIN) {
            return false;
        }

        return true; // Proceed with the request
    }
}
