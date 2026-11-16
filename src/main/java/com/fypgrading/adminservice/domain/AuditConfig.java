package com.fypgrading.adminservice.domain;

import com.fypgrading.adminservice.common.security.CurrentUser;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Fills {@link Audited}'s columns: the user from the token (never "SYSTEM" for a person), the time from the clock. */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "currentAuditor", dateTimeProviderRef = "auditClock")
public class AuditConfig {

    @Bean
    AuditorAware<UUID> currentAuditor() {
        return () -> CurrentUser.find().map(CurrentUser::id);
    }

    @Bean
    DateTimeProvider auditClock(Clock clock) {
        return () -> Optional.of(Instant.now(clock));
    }
}
