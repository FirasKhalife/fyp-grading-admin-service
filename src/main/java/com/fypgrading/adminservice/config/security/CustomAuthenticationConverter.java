package com.fypgrading.adminservice.config.security;

import com.fypgrading.adminservice.entity.Reviewer;
import com.fypgrading.adminservice.service.ReviewerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@Slf4j
@AllArgsConstructor
public class CustomAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final ClientRegistration clientRegistration;
    private final RestTemplate restTemplate;
    private final ReviewerService reviewerService;

    @Transactional
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Map<String, Object> claims = jwt.getClaims();
        UUID id = UUID.fromString((String) claims.get("sub"));

        Reviewer reviewer = reviewerService.findById(id)
            .map(foundReviewer -> updateAuthorities(foundReviewer, jwt))
            .orElseGet(() -> {
                // first authentication -> fetch user info from the provider and save user
                Map<String, Object> userInfo = fetchUserInfo(jwt);

                if (userInfo == null)
                    throw new IllegalStateException("Unable to fetch user info from OAuth2 provider");

                log.info("Found User Info: {}", userInfo);

                Reviewer user = reviewerService.createReviewerFromAuthentication(userInfo);
                Collection<GrantedAuthority> authorities = AuthoritiesConverter.convertAuthorities(jwt);

                // if several endpoints are triggered at the same time by a non created user
                // a DataIntegrityViolationException will be thrown, caused by trying to insert two rows with the same login
                try {
                    return reviewerService.saveAuthorities(user, authorities);
                } catch (DataIntegrityViolationException e) { // user already exists
                    return reviewerService.getById(id);
                }
            });

        return UsernamePasswordAuthenticationToken.authenticated(reviewer, null, reviewer.getRoles());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchUserInfo(Jwt jwt) {
        return restTemplate.exchange(
            clientRegistration.getProviderDetails().getUserInfoEndpoint().getUri(),
            HttpMethod.GET,
            new HttpEntity<>(SecurityUtils.buildBearer(jwt.getTokenValue())),
            Map.class
        ).getBody();
    }

    private Reviewer updateAuthorities(Reviewer reviewer, Jwt jwt) {
        Collection<GrantedAuthority> authorities = AuthoritiesConverter.convertAuthorities(jwt);
        return reviewerService.saveAuthorities(reviewer, authorities);
    }
}
