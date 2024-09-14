package com.fypgrading.adminservice.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.support.DatabaseStartupValidator;

import javax.sql.DataSource;
import java.util.stream.Stream;

@Configuration
public class DatabaseConfig {

    @Bean
    public DatabaseStartupValidator databaseStartupValidator(DataSource dataSource) {
        var dsv = new DatabaseStartupValidator();
        dsv.setDataSource(dataSource);
        dsv.setTimeout(60);
        dsv.setInterval(7);
        dsv.setValidationQuery(DatabaseDriver.POSTGRESQL.getValidationQuery());
        return dsv;
    }

    @Bean
    public static BeanFactoryPostProcessor dependsOnPostProcessor() {
        return bf -> {
            // Make JPA EntityManagerFactory depend on the DatabaseStartupValidator
            String[] jpa = bf.getBeanNamesForType(EntityManagerFactory.class);
            Stream.of(jpa)
                    .map(bf::getBeanDefinition)
                    .forEach(it -> it.setDependsOn("databaseStartupValidator"));
        };
    }
}
