package com.school.app.config;

import com.school.app.dashboard.DataHealthReport;
import com.school.app.dashboard.DataHealthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Prints, once at startup, WHICH database the app is connected to and how many
 * rows it can actually see in each table - plus a warning for anything that
 * would leave a dropdown empty. Runs after {@link LegacyDataRepair}.
 * Read-only and never fails startup.
 */
@Component
@Order(20)
public class StartupDiagnostics implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupDiagnostics.class);

    private final DataHealthService dataHealthService;

    public StartupDiagnostics(DataHealthService dataHealthService) {
        this.dataHealthService = dataHealthService;
    }

    @Override
    public void run(String... args) {
        try {
            DataHealthReport report = dataHealthService.buildReport();
            log.info("DATA CHECK - connected to database '{}' ({})", report.getDatabaseName(), report.getJdbcUrl());
            log.info("DATA CHECK - rows visible to the app: {}", report.getCounts());
            if (report.getProblems().isEmpty()) {
                log.info("DATA CHECK - no problems found.");
            }
            for (String problem : report.getProblems()) {
                log.warn("DATA CHECK - {}", problem);
            }
        } catch (Exception ex) {
            log.warn("DATA CHECK could not run: {}", ex.toString());
        }
    }
}
