package com.dezxxx.rest.util;

import org.flywaydb.core.Flyway;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FlyWayConfig {

    private static final Logger log = LoggerFactory.getLogger(FlyWayConfig.class);

    private FlyWayConfig() {
    }

    public static void runMigrations() {
        Configuration cfg = new Configuration().configure();
        Flyway flyway = Flyway.configure()
                .dataSource(
                        cfg.getProperty("hibernate.connection.url"),
                        cfg.getProperty("hibernate.connection.username"),
                        cfg.getProperty("hibernate.connection.password")
                )
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();
        log.info("Flyway migration completed");
    }
}