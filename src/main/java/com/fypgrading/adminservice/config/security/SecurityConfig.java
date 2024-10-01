package com.fypgrading.adminservice.config.security;

import com.fypgrading.adminservice.service.ReviewerService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;

@AllArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final ReviewerService reviewerService;
    private final RestTemplate restTemplate;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(request -> request
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                // TODO: revert when done
                // .requestMatchers("/actuator/shutdown").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET).permitAll()
                .anyRequest().authenticated())
            .oauth2Client(Customizer.withDefaults())
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwtSpec -> jwtSpec.jwtAuthenticationConverter(
                        new CustomAuthenticationConverter(
                            clientRegistrationRepository.findByRegistrationId("master"),
                            restTemplate, reviewerService))))
            .csrf(AbstractHttpConfigurer::disable)
            .build();
    }
}
