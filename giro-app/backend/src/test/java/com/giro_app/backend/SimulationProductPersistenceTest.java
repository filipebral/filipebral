package com.giro_app.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.UUID;
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
@Import({SimulationService.class, SimulationCardService.class, SimulationFinanceService.class,
        InAppNotificationService.class, SensitiveDataConverter.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class SimulationProductPersistenceTest {
    @Autowired private AppUserRepository users;
    @Autowired private SimulationService simulation;
    @Autowired private SimulationCardService cards;
    @Autowired private SimulationFinanceService finance;
    @Autowired private InAppNotificationService notifications;

    @Test
    void creditAndDebitCardPurchasesRespectLimitBlockAndSimulatedCash() {
        UUID user = createUser();
        cards.issue(user, new BigDecimal("300.00"));
        UUID creditRequest = UUID.randomUUID();
        cards.purchase(user, creditRequest, "CREDIT", new BigDecimal("100.00"), "Loja fictícia");
        cards.purchase(user, creditRequest, "CREDIT", new BigDecimal("100.00"), "Loja fictícia");
        assertEquals(new BigDecimal("200.00"), cards.get(user).availableLimit());
        assertEquals(new BigDecimal("500.00"), simulation.account(user).balance());

        UUID debitRequest = UUID.randomUUID();
        cards.purchase(user, debitRequest, "DEBIT", new BigDecimal("50.00"), "Mercado fictício");
        assertEquals(new BigDecimal("450.00"), simulation.account(user).balance());

        cards.setBlocked(user, true);
        assertThrows(ResponseStatusException.class,
                () -> cards.purchase(user, UUID.randomUUID(), "CREDIT", BigDecimal.ONE, "Loja"));
        cards.purchase(user, creditRequest, "CREDIT", new BigDecimal("100.00"), "Loja fictícia");
        cards.setBlocked(user, false);

        var statement = cards.listStatements(user).get(0);
        cards.pay(user, statement.month(), UUID.randomUUID());
        assertEquals("PAID", cards.listStatements(user).get(0).status());
        assertEquals(new BigDecimal("350.00"), simulation.account(user).balance());
        assertEquals(4, notifications.list(user).size());
    }

    @Test
    void goalsReserveOnlyAvailableBalanceAndBudgetsNotifyAtThresholds() {
        UUID user = createUser();
        var goal = finance.createGoal(user, "Viagem", new BigDecimal("300.00"));
        UUID contribution = UUID.randomUUID();
        finance.contribute(user, goal.id(), contribution, new BigDecimal("200.00"));
        finance.contribute(user, goal.id(), contribution, new BigDecimal("200.00"));
        assertEquals(new BigDecimal("300.00"), simulation.account(user).availableBalance());
        assertThrows(ResponseStatusException.class,
                () -> simulation.operate(user, UUID.randomUUID(), new BigDecimal("301.00"), "PIX", "Fictício"));

        String month = java.time.YearMonth.now().toString();
        finance.setBudget(user, month, "FOOD", new BigDecimal("100.00"));
        simulation.operate(user, UUID.randomUUID(), new BigDecimal("80.00"), "PIX", "Restaurante fictício");
        var firstTransaction = simulation.statement(user).get(0);
        finance.categorizeTransaction(user, firstTransaction.id(), "FOOD");
        assertEquals(new BigDecimal("80.00"), finance.budgets(user, month).get(0).spent());
        assertEquals(2, notifications.list(user).size());

        simulation.operate(user, UUID.randomUUID(), new BigDecimal("20.00"), "TRANSFER", "Outro destino fictício");
        finance.categorizeTransaction(user, simulation.statement(user).get(0).id(), "FOOD");
        assertEquals(new BigDecimal("100.00"), finance.budgets(user, month).get(0).spent());
        assertEquals(3, notifications.list(user).size());

        finance.release(user, goal.id(), UUID.randomUUID(), new BigDecimal("50.00"));
        assertEquals(new BigDecimal("250.00"), simulation.account(user).availableBalance());
        assertEquals(new BigDecimal("150.00"), finance.goals(user).get(0).savedAmount());
    }

    private UUID createUser() {
        UUID user = users.saveAndFlush(new AppUser(null, null, "Cliente", "hash")).getId();
        simulation.create(user);
        simulation.operate(user, UUID.randomUUID(), new BigDecimal("500.00"), "CREDIT", null);
        return user;
    }
}
