package com.fypgrading.adminservice.config.security;

import com.fypgrading.adminservice.entity.Reviewer;
import com.fypgrading.adminservice.service.enums.SystemRoleEnum;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.*;
import java.util.stream.Stream;

/**
 * Utility class for Spring Security.
 */
public final class SecurityUtils {

    public static Reviewer getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Reviewer) auth.getPrincipal();
    }

    public static Optional<String> getCurrentUserEmail() {
        return Optional.of(getCurrentUser().getEmail());
    }

    public static UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public static HttpHeaders buildBearer(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }

    public static boolean isUserAdmin(Reviewer reviewer) {
        return reviewer.getRoles()
            .stream().anyMatch(role -> Objects.equals(role.getName(), SystemRoleEnum.ROLE_ADMIN.toString()));
    }

    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && getAuthorities(authentication).noneMatch("ROLE_ANONYMOUS"::equals);
    }

    private static Stream<String> getAuthorities(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication instanceof JwtAuthenticationToken
            ? getAuthoritiesFromClaims(((JwtAuthenticationToken) authentication).getToken().getClaims())
            : authentication.getAuthorities();
        return authorities.stream().map(GrantedAuthority::getAuthority);
    }

    public static Collection<GrantedAuthority> getAuthoritiesFromClaims(Map<String, Object> claims) {
        return getAuthorityNamesFromClaims(claims)
            .stream()
            .map(roleName -> (GrantedAuthority) new SimpleGrantedAuthority(roleName))
            .toList();
    }

    @SuppressWarnings("unchecked")
    public static Collection<String> getAuthorityNamesFromClaims(Map<String, Object> claims) {
        Map<String, Object> realmAccess = (Map<String, Object>) claims.get("realm");
        if (realmAccess == null || realmAccess.isEmpty())
            return new ArrayList<>();

        return ((List<String>) realmAccess.get("roles"))
            .stream()
            .filter(roleName -> roleName.startsWith("ROLE_"))
            .toList();
    }
}
