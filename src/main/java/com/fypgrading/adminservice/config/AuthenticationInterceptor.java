package com.fypgrading.adminservice.config;

import com.fypgrading.adminservice.repository.ReviewerRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthenticationInterceptor implements HandlerInterceptor {

    private final ReviewerRepository reviewerRepository;

    public AuthenticationInterceptor(ReviewerRepository reviewerRepository) {
        this.reviewerRepository = reviewerRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // Extract username and password from the request
        String username = request.getHeader("email");
        String password = request.getHeader("password");

        // Perform authentication logic (e.g., query the database)
        if (reviewerRepository.findByEmailAndPassword(username, password).isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized");
            return false; // Stop further processing of the request
        }

        return true; // Proceed with the request
    }
}
