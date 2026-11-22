package com.giro_app.backend;

import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@ConditionalOnProperty(name = "app.simulation.enabled", havingValue = "true", matchIfMissing = true)
public class SimulationScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimulationScheduler.class);
    private final SimulationScheduleRepository schedules;
    private final SimulationScheduleService service;

    public SimulationScheduler(SimulationScheduleRepository schedules, SimulationScheduleService service) {
        this.schedules = schedules;
        this.service = service;
    }

    @Scheduled(fixedDelayString = "${app.simulation.scheduler-delay-ms:30000}")
    public void executeDue() {
        for (SimulationSchedule schedule : schedules.findTop100ByStatusAndScheduledAtLessThanEqualOrderByScheduledAtAsc("PENDING", Instant.now())) {
            try {
                service.execute(schedule.getId());
            } catch (RuntimeException ex) {
                LOGGER.error("Não foi possível executar o agendamento simulado {}", schedule.getId(), ex);
            }
        }
    }
}
