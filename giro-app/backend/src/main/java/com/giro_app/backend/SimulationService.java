package com.giro_app.backend;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@ConditionalOnProperty(name = "app.simulation.enabled", havingValue = "true", matchIfMissing = true)
public class SimulationService {
    private static final BigDecimal MAX_BALANCE = new BigDecimal("999999999.99");
    private final AppUserRepository users;
    private final SimulationAccountRepository accounts;
    private final SimulationTransactionRepository transactions;

    public SimulationService(AppUserRepository users, SimulationAccountRepository accounts,
                             SimulationTransactionRepository transactions) {
        this.users = users;
        this.accounts = accounts;
        this.transactions = transactions;
    }

    @Transactional
    public AccountView create(UUID userId) {
        users.findForUpdate(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado."));
        SimulationAccount account = accounts.findById(userId)
                .orElseGet(() -> accounts.save(new SimulationAccount(userId)));
        return new AccountView(account.getBalance());
    }

    @Transactional(readOnly = true)
    public AccountView account(UUID userId) {
        return new AccountView(requireAccount(userId).getBalance());
    }

    @Transactional(readOnly = true)
    public List<TransactionView> statement(UUID userId) {
        requireAccount(userId);
        return transactions.findTop50ByUserIdOrderByCreatedAtDescIdDesc(userId)
                .stream().map(TransactionView::from).toList();
    }

    @Transactional
    public AccountView operate(UUID userId, UUID requestId, BigDecimal amount, String type, String destination) {
        String description = validateOperation(requestId, amount, type, destination);
        amount = amount.setScale(2, RoundingMode.UNNECESSARY);

        SimulationAccount account = accounts.findForUpdate(userId).orElseThrow(SimulationService::accountNotFound);
        var existing = transactions.findByUserIdAndRequestId(userId, requestId);
        if (existing.isPresent()) {
            SimulationTransaction previous = existing.get();
            if (!previous.getType().equals(type) || previous.getAmount().compareTo(amount) != 0
                    || !previous.getDescription().equals(description)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Identificador já usado para outra operação.");
            }
            return new AccountView(account.getBalance());
        }

        BigDecimal newBalance = "CREDIT".equals(type)
                ? account.getBalance().add(amount) : account.getBalance().subtract(amount);
        if (newBalance.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Saldo fictício insuficiente.");
        }
        if (newBalance.compareTo(MAX_BALANCE) > 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Limite de saldo fictício excedido.");
        }
        account.setBalance(newBalance);
        transactions.save(new SimulationTransaction(userId, requestId, type, amount, description));
        return new AccountView(newBalance);
    }

    static String validateOperation(UUID requestId, BigDecimal amount, String type, String destination) {
        if (requestId == null || amount == null || amount.signum() <= 0 || amount.scale() > 2
                || amount.compareTo(MAX_BALANCE) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um valor positivo com até dois centavos e um identificador válido.");
        }
        String description;
        if ("CREDIT".equals(type)) {
            description = "Adição de saldo fictício";
        } else if ("PIX".equals(type) || "TRANSFER".equals(type) || "BILL".equals(type)) {
            if (destination == null || destination.isBlank() || destination.trim().length() > 100) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um nome fictício de até 100 caracteres.");
            }
            description = destination.trim();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Operação inválida.");
        }

        return description;
    }

    private SimulationAccount requireAccount(UUID userId) {
        return accounts.findById(userId).orElseThrow(SimulationService::accountNotFound);
    }

    private static ResponseStatusException accountNotFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta simulada não encontrada.");
    }

    public record AccountView(BigDecimal balance) {}
    public record TransactionView(UUID id, String type, BigDecimal amount, String description, Instant createdAt) {
        static TransactionView from(SimulationTransaction transaction) {
            return new TransactionView(transaction.getId(), transaction.getType(), transaction.getAmount(),
                    transaction.getDescription(), transaction.getCreatedAt());
        }
    }
}
