package com.fypgrading.adminservice.common.jpa;

import com.fypgrading.adminservice.common.demo.DemoSession;
import java.util.Map;
import java.util.UUID;
import org.hibernate.cfg.MultiTenancySettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

/**
 * Hibernate's discriminator multi-tenancy, keyed on the demo session: every entity with a {@code @TenantId}
 * column is filtered and stamped automatically. Without a session (jobs) the resolver returns a sentinel that
 * matches no copy (review M7): code that forgets to bind a session sees nothing and its writes land in no visitor's
 * copy, instead of reading and changing everyone's. Cross-copy work (the midnight purge) uses plain SQL.
 */
@Component
public class DemoTenantResolver implements CurrentTenantIdentifierResolver<String>, HibernatePropertiesCustomizer {

    /** Not a UUID, so it never equals a session; and never the root tenant. */
    public static final String NO_SESSION = "no-session";

    @Override
    public String resolveCurrentTenantIdentifier() {
        return DemoSession.current().map(UUID::toString).orElse(NO_SESSION);
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return false;
    }

    @Override
    public boolean isRoot(String tenantId) {
        return false;
    }

    @Override
    public void customize(Map<String, Object> properties) {
        properties.put(MultiTenancySettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
    }
}
