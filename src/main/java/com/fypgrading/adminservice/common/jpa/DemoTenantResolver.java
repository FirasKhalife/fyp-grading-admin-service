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
 * column is filtered and stamped automatically. Without a session (jobs) the resolver returns the root
 * tenant, which sees every copy; nothing is ever written as root.
 */
@Component
public class DemoTenantResolver implements CurrentTenantIdentifierResolver<String>, HibernatePropertiesCustomizer {

    public static final String ROOT = "root";

    @Override
    public String resolveCurrentTenantIdentifier() {
        return DemoSession.current().map(UUID::toString).orElse(ROOT);
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return false;
    }

    @Override
    public boolean isRoot(String tenantId) {
        return ROOT.equals(tenantId);
    }

    @Override
    public void customize(Map<String, Object> properties) {
        properties.put(MultiTenancySettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
    }
}
