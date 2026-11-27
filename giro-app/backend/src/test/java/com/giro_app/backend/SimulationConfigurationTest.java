package com.giro_app.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.sql.DriverManager;
import java.util.Map;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;

class SimulationConfigurationTest {
    @Test
    void simulationCanBeExplicitlyDisabled() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().setActiveProfiles("prod");
            context.getEnvironment().getPropertySources().addFirst(
                    new MapPropertySource("test", Map.of("app.simulation.enabled", "false")));
            context.register(SimulationController.class, SimulationService.class,
                    SimulationReconciliationController.class, SimulationReconciliationService.class,
                    SimulationReconciliationScheduler.class);
            context.refresh();
            assertFalse(context.containsBeanDefinition("simulationController"));
            assertFalse(context.containsBeanDefinition("simulationService"));
            assertFalse(context.containsBeanDefinition("simulationReconciliationController"));
            assertFalse(context.containsBeanDefinition("simulationReconciliationService"));
            assertFalse(context.containsBeanDefinition("simulationReconciliationScheduler"));
        }
    }

    @Test
    void migrationCreatesSimulationTables() throws Exception {
        String url = "jdbc:h2:mem:simulation_migrations;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        try (var connection = DriverManager.getConnection(url, "sa", "");
             var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE app_users (id UUID PRIMARY KEY)");
            statement.execute("CREATE TABLE split_transactions (id UUID PRIMARY KEY)");
        }
        Flyway flyway = Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("3")
                .load();
        assertEquals(12, flyway.migrate().migrationsExecuted);
        assertEquals("15", flyway.info().current().getVersion().getVersion());
    }
}
