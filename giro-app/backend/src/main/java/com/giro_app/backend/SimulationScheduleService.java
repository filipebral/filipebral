package com.giro_app.backend;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@ConditionalOnProperty(name = "app.simulation.enabled", havingValue = "true", matchIfMissing = true)
public class SimulationScheduleService {
    private final SimulationAccountRepository accounts;
    private final SimulationScheduleRepository schedules;
    private final SimulationService operations;

    public SimulationScheduleService(SimulationAccountRepository accounts, SimulationScheduleRepository schedules,
                                     SimulationService operations) {
        this.accounts = accounts;
        this.schedules = schedules;
        this.operations = operations;
    }

    @Transactional
    public ScheduleView create(UUID userId, UUID requestId, BigDecimal amount, String destination, Instant scheduledAt) {
        String name = SimulationService.validateOperation(requestId, amount, "PIX", destination);
        if (scheduledAt != null) scheduledAt = scheduledAt.truncatedTo(ChronoUnit.MICROS);
        accounts.findForUpdate(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta simulada não encontrada."));
        var existing = schedules.findByUserIdAndRequestId(userId, requestId);
        if (existing.isPresent()) {
            SimulationSchedule schedule = existing.get();
            if (schedule.getAmount().compareTo(amount) != 0 || !schedule.getDestination().equals(name)
                    || !schedule.getScheduledAt().equals(scheduledAt)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Identificador já usado para outro agendamento.");
            }
            return ScheduleView.from(schedule);
        }
        if (scheduledAt == null || !scheduledAt.isAfter(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe uma data futura para o agendamento.");
        }
        return ScheduleView.from(schedules.save(new SimulationSchedule(userId, requestId, amount, name, scheduledAt)));
    }

    @Transactional(readOnly = true)
    public List<ScheduleView> list(UUID userId) {
        return schedules.findTop50ByUserIdOrderByScheduledAtDesc(userId).stream().map(ScheduleView::from).toList();
    }

    @Transactional
    public ScheduleView cancel(UUID userId, UUID id) {
        SimulationSchedule schedule = schedules.findForUpdate(id).filter(item -> item.getUserId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agendamento não encontrado."));
        if ("CANCELLED".equals(schedule.getStatus())) return ScheduleView.from(schedule);
        if (!"PENDING".equals(schedule.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O agendamento já foi processado.");
        }
        schedule.setStatus("CANCELLED");
        return ScheduleView.from(schedule);
    }

    @Transactional
    public void execute(UUID id) {
        SimulationSchedule schedule = schedules.findForUpdate(id).orElseThrow();
        if (!"PENDING".equals(schedule.getStatus()) || schedule.getScheduledAt().isAfter(Instant.now())) return;
        SimulationAccount account = accounts.findForUpdate(schedule.getUserId()).orElseThrow();
        if (account.getBalance().compareTo(schedule.getAmount()) < 0) {
            schedule.setStatus("FAILED");
            return;
        }
        operations.operate(schedule.getUserId(), schedule.getExecutionId(), schedule.getAmount(), "PIX", schedule.getDestination());
        schedule.setStatus("COMPLETED");
    }

    public record ScheduleView(UUID id, BigDecimal amount, String destination, Instant scheduledAt, String status) {
        static ScheduleView from(SimulationSchedule schedule) {
            return new ScheduleView(schedule.getId(), schedule.getAmount(), schedule.getDestination(),
                    schedule.getScheduledAt(), schedule.getStatus());
        }
    }
}
