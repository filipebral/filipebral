package com.giro_app.backend;

import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.simulation.enabled", havingValue = "true", matchIfMissing = true)
public class SimulationReconciliationScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimulationReconciliationScheduler.class);
    private final SimulationReconciliationRepository queue;
    private final SimulationReconciliationService service;

    SimulationReconciliationScheduler(SimulationReconciliationRepository queue, SimulationReconciliationService service) {
        this.queue = queue;
        this.service = service;
    }

    @Scheduled(fixedDelayString = "${app.simulation.scheduler-delay-ms:30000}")
    public void checkDue() {
        for (SimulationReconciliation item : queue
                .findTop100ByStatusAndNextCheckAtLessThanEqualOrderByNextCheckAtAsc("UNKNOWN", Instant.now())) {
            try {
                service.check(item.getId());
            } catch (RuntimeException exception) {
                LOGGER.error("Não foi possível consultar a operação simulada {}", item.getId(), exception);
            }
        }
    }
}
