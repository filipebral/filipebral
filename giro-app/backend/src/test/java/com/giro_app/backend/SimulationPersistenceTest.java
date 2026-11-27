package com.giro_app.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "app.security.encryption.key-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
})
@ActiveProfiles("test")
@Import({SimulationService.class, SimulationScheduleService.class, SimulationBillService.class, SensitiveDataConverter.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class SimulationPersistenceTest {
    @Autowired private AppUserRepository users;
    @Autowired private SimulationService service;
    @Autowired private SimulationScheduleService scheduleService;
    @Autowired private SimulationScheduleRepository schedules;
    @Autowired private SimulationBillService billService;
    @Autowired private SimulationAccountRepository accounts;
    @Autowired private AuditEventRepository auditEvents;

    @Test
    void persistsSeparatelyAndSerializesConcurrentDebits() throws Exception {
        UUID user = users.saveAndFlush(new AppUser(null, null, "Teste", "hash")).getId();
        UUID other = users.saveAndFlush(new AppUser(null, null, "Outro", "hash")).getId();
        service.create(user);
        service.create(other);
        UUID creditId = UUID.randomUUID();
        service.operate(user, creditId, new BigDecimal("10.00"), "CREDIT", null);
        service.operate(user, creditId, new BigDecimal("10.00"), "CREDIT", null);

        var executor = Executors.newFixedThreadPool(4);
        try {
            List<Callable<Boolean>> tasks = new ArrayList<>();
            for (int i = 0; i < 12; i++) {
                tasks.add(() -> {
                    try {
                        service.operate(user, UUID.randomUUID(), BigDecimal.ONE, "PIX", "Fictício");
                        return true;
                    } catch (ResponseStatusException ex) {
                        if (ex.getStatusCode().value() != 422) throw ex;
                        return false;
                    }
                });
            }
            int successful = 0;
            for (var result : executor.invokeAll(tasks)) {
                if (result.get()) successful++;
            }
            assertEquals(10, successful);
        } finally {
            executor.shutdown();
        }

        assertEquals(new BigDecimal("0.00"), service.account(user).balance());
        assertEquals(11, service.statement(user).size());
        assertEquals(0, service.statement(other).size());
        assertEquals(new BigDecimal("0.00"), service.account(other).balance());
        assertThrows(ResponseStatusException.class, () -> service.account(UUID.randomUUID()));
    }

    @Test
    void executesScheduledPixOnlyOnceAndMarksInsufficientBalance() {
        UUID user = users.saveAndFlush(new AppUser(null, null, "Agenda", "hash")).getId();
        service.create(user);
        service.operate(user, UUID.randomUUID(), new BigDecimal("5.00"), "CREDIT", null);
        SimulationSchedule due = schedules.saveAndFlush(new SimulationSchedule(
                user, UUID.randomUUID(), new BigDecimal("3.00"), "Fictício", Instant.now().minusSeconds(1)));
        scheduleService.execute(due.getId());
        scheduleService.execute(due.getId());
        assertEquals(new BigDecimal("2.00"), service.account(user).balance());
        assertEquals("COMPLETED", schedules.findById(due.getId()).orElseThrow().getStatus());
        assertEquals(2, service.statement(user).size());

        SimulationSchedule insufficient = schedules.saveAndFlush(new SimulationSchedule(
                user, UUID.randomUUID(), new BigDecimal("3.00"), "Fictício", Instant.now().minusSeconds(1)));
        scheduleService.execute(insufficient.getId());
        assertEquals("FAILED", schedules.findById(insufficient.getId()).orElseThrow().getStatus());
        assertEquals(new BigDecimal("2.00"), service.account(user).balance());
        assertEquals(2, service.statement(user).size());
    }

    @Test
    void scheduledPixFailsWhenAvailableBalanceIsReserved() {
        UUID user = users.saveAndFlush(new AppUser(null, null, "Reserva", "hash")).getId();
        service.create(user);
        service.operate(user, UUID.randomUUID(), new BigDecimal("10.00"), "CREDIT", null);
        SimulationAccount account = accounts.findById(user).orElseThrow();
        account.setReservedBalance(new BigDecimal("8.00"));
        accounts.saveAndFlush(account);
        SimulationSchedule due = schedules.saveAndFlush(new SimulationSchedule(
                user, UUID.randomUUID(), new BigDecimal("3.00"), "Fictício", Instant.now().minusSeconds(1)));
        scheduleService.execute(due.getId());
        assertEquals("FAILED", schedules.findById(due.getId()).orElseThrow().getStatus());
        assertEquals(new BigDecimal("10.00"), service.account(user).balance());
        assertEquals(1, service.statement(user).size());
    }

    @Test
    void operationLookupAndHistoryAreScopedAndIdempotent() {
        UUID user = users.saveAndFlush(new AppUser(null, null, "Titular", "hash")).getId();
        UUID other = users.saveAndFlush(new AppUser(null, null, "Outro", "hash")).getId();
        service.create(user);
        service.create(other);
        UUID request = UUID.randomUUID();
        service.operate(user, request, new BigDecimal("10.00"), "CREDIT", null);
        service.operate(user, request, new BigDecimal("10.00"), "CREDIT", null);
        service.operate(user, UUID.randomUUID(), BigDecimal.ONE, "PIX", "Fictício");

        assertEquals("CREDIT", service.operation(user, request).type());
        assertThrows(ResponseStatusException.class, () -> service.operation(other, request));
        var firstPage = service.history(user, 0, 1);
        assertEquals(1, firstPage.items().size());
        assertEquals(true, firstPage.hasNext());
        assertEquals(1, service.history(user, 1, 1).items().size());
        assertEquals(0, service.history(other, 0, 10).items().size());
        assertEquals(2, auditEvents.findAll().stream()
                .filter(event -> Objects.equals(event.getActorUserId(), user)).count());
    }

    @Test
    void creationIsIdempotentAndOnlyOwnerCanCancel() {
        UUID user = users.saveAndFlush(new AppUser(null, null, "Titular", "hash")).getId();
        service.create(user);
        UUID requestId = UUID.randomUUID();
        Instant time = Instant.now().plusSeconds(600);
        var first = scheduleService.create(user, requestId, BigDecimal.ONE, "Fictício", time);
        var retry = scheduleService.create(user, requestId, BigDecimal.ONE, "Fictício", time);
        assertEquals(first.id(), retry.id());
        assertThrows(ResponseStatusException.class,
                () -> scheduleService.create(user, requestId, BigDecimal.TEN, "Fictício", time));
        assertThrows(ResponseStatusException.class, () -> scheduleService.cancel(UUID.randomUUID(), first.id()));
        assertEquals("CANCELLED", scheduleService.cancel(user, first.id()).status());
        assertEquals("CANCELLED", scheduleService.cancel(user, first.id()).status());
        scheduleService.execute(first.id());
        assertEquals("CANCELLED", scheduleService.list(user).get(0).status());
        assertEquals(1, auditEvents.findAll().stream().filter(event ->
                Objects.equals(event.getResourceId(), first.id())
                        && event.getEventType().equals("SIMULATION_SCHEDULE_CREATED")).count());
        assertEquals(1, auditEvents.findAll().stream().filter(event ->
                Objects.equals(event.getResourceId(), first.id())
                        && event.getEventType().equals("SIMULATION_SCHEDULE_CANCELLED")).count());
    }

    @Test
    void billPaymentIsPrivateAtomicAndIdempotent() {
        UUID user = users.saveAndFlush(new AppUser(null, null, "Boletos", "hash")).getId();
        service.create(user);
        UUID request = UUID.randomUUID();
        LocalDate due = LocalDate.now().plusDays(1);
        var bill = billService.create(user, request, "Boleto fictício", new BigDecimal("3.00"), due);
        assertEquals(bill.id(), billService.create(user, request, "Boleto fictício", new BigDecimal("3.00"), due).id());
        assertThrows(ResponseStatusException.class, () -> billService.pay(UUID.randomUUID(), bill.id()));
        assertThrows(ResponseStatusException.class, () -> billService.pay(user, bill.id()));
        assertEquals("PENDING", billService.list(user).get(0).status());
        service.operate(user, UUID.randomUUID(), new BigDecimal("5.00"), "CREDIT", null);
        assertEquals("PAID", billService.pay(user, bill.id()).status());
        billService.pay(user, bill.id());
        assertEquals(new BigDecimal("2.00"), service.account(user).balance());
        assertEquals(2, service.statement(user).size());
        assertEquals(1, auditEvents.findAll().stream().filter(event ->
                Objects.equals(event.getResourceId(), bill.id())
                        && event.getEventType().equals("SIMULATION_BILL_PAID")).count());
    }
}
