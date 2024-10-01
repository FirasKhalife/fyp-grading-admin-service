package com.fypgrading.adminservice.config.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AuthoritiesConverter {

    @SuppressWarnings("unchecked")
    public static Collection<GrantedAuthority> convertAuthorities(Jwt jwt) {
        Map<String, Object> realmAccess = (Map<String, Object>) jwt.getClaims().get("realm");
        if (realmAccess == null || realmAccess.isEmpty())
            return new ArrayList<>();

        return ((List<String>) realmAccess.get("roles"))
            .stream()
            .filter(roleName -> roleName.startsWith("ROLE_"))
            .map(roleName -> (GrantedAuthority) new SimpleGrantedAuthority(roleName))
            .toList();
    }
}
