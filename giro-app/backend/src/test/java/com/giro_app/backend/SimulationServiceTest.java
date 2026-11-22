package com.giro_app.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class SimulationServiceTest {
    private final AppUserRepository users = org.mockito.Mockito.mock(AppUserRepository.class);
    private final SimulationAccountRepository accounts = org.mockito.Mockito.mock(SimulationAccountRepository.class);
    private final SimulationTransactionRepository transactions = org.mockito.Mockito.mock(SimulationTransactionRepository.class);
    private final SimulationService service = new SimulationService(users, accounts, transactions);
    private final UUID user = UUID.randomUUID();
    private final UUID requestId = UUID.randomUUID();
    private final SimulationAccount account = new SimulationAccount(user);

    @BeforeEach
    void setUp() {
        when(accounts.findForUpdate(user)).thenReturn(Optional.of(account));
    }

    @Test
    void addsFictitiousCreditAndDebitsPix() {
        service.operate(user, requestId, new BigDecimal("25.50"), "CREDIT", null);
        service.operate(user, UUID.randomUUID(), new BigDecimal("10.25"), "PIX", "Cliente fictício");
        assertEquals(new BigDecimal("15.25"), account.getBalance());
        verify(transactions, org.mockito.Mockito.times(2)).save(any(SimulationTransaction.class));
    }

    @Test
    void rejectsInsufficientFundsWithoutRecordingTransaction() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.operate(user, requestId, BigDecimal.ONE, "PIX", "Cliente fictício"));
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, error.getStatusCode());
        assertEquals(new BigDecimal("0.00"), account.getBalance());
        verify(transactions, never()).save(any());
    }

    @Test
    void transferDebitsOnlyTheSimulatedAccount() {
        account.setBalance(new BigDecimal("10.00"));
        service.operate(user, requestId, new BigDecimal("4.00"), "TRANSFER", "Destinatário fictício");
        assertEquals(new BigDecimal("6.00"), account.getBalance());
        verify(transactions).save(any(SimulationTransaction.class));
    }

    @Test
    void retryDoesNotApplyAnOperationTwiceAndConflictingReuseFails() {
        account.setBalance(new BigDecimal("5.00"));
        when(transactions.findByUserIdAndRequestId(user, requestId)).thenReturn(Optional.of(
                new SimulationTransaction(user, requestId, "CREDIT", new BigDecimal("5.00"), "Adição de saldo fictício")));
        service.operate(user, requestId, new BigDecimal("5.00"), "CREDIT", null);
        assertEquals(new BigDecimal("5.00"), account.getBalance());
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.operate(user, requestId, new BigDecimal("6.00"), "CREDIT", null));
        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(transactions, never()).save(any());
    }

    @Test
    void rejectsInvalidAmountsAndMissingDestination() {
        assertThrows(ResponseStatusException.class,
                () -> service.operate(user, requestId, new BigDecimal("0.001"), "CREDIT", null));
        assertThrows(ResponseStatusException.class,
                () -> service.operate(user, requestId, BigDecimal.ZERO, "CREDIT", null));
        assertThrows(ResponseStatusException.class,
                () -> service.operate(user, requestId, BigDecimal.ONE, "PIX", " "));
        verify(transactions, never()).save(any());
    }

    @Test
    void cannotOperateOnAnotherUsersAccount() {
        UUID other = UUID.randomUUID();
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.operate(other, requestId, BigDecimal.ONE, "CREDIT", null));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verify(transactions, never()).save(any());
    }
}
