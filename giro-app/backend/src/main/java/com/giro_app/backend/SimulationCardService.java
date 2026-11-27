package com.giro_app.backend;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@ConditionalOnProperty(name = "app.simulation.enabled", havingValue = "true", matchIfMissing = true)
public class SimulationCardService {
    private static final BigDecimal MAX_LIMIT = new BigDecimal("999999999.99");
    private final SimulationCardRepository cards;
    private final SimulationCardStatementRepository statements;
    private final SimulationCardTransactionRepository transactions;
    private final SimulationAccountRepository accounts;
    private final SimulationService operations;
    private final InAppNotificationService notifications;
    private final AuditEventRepository auditEvents;

    public SimulationCardService(SimulationCardRepository cards, SimulationCardStatementRepository statements,
            SimulationCardTransactionRepository transactions, SimulationAccountRepository accounts,
            SimulationService operations, InAppNotificationService notifications, AuditEventRepository auditEvents) {
        this.cards = cards;
        this.statements = statements;
        this.transactions = transactions;
        this.accounts = accounts;
        this.operations = operations;
        this.notifications = notifications;
        this.auditEvents = auditEvents;
    }

    @Transactional(readOnly = true)
    public CardView get(UUID userId) {
        SimulationCard card = cards.findById(userId).orElse(null);
        if (card == null) return new CardView(false, null, null, null);
        BigDecimal outstanding = statements.findByUserIdAndStatusOrderByStatementMonthDesc(userId, "OPEN").stream()
                .map(SimulationCardStatement::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CardView(true, card.getCreditLimit(), card.isBlocked(),
                card.getCreditLimit().subtract(outstanding).max(BigDecimal.ZERO));
    }

    @Transactional
    public CardView issue(UUID userId, BigDecimal creditLimit) {
        validateAmount(creditLimit);
        accounts.findForUpdate(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta simulada não encontrada."));
        SimulationCard card = cards.findForUpdate(userId).orElseGet(() -> new SimulationCard(userId, creditLimit));
        if (cards.existsById(userId)) {
            BigDecimal outstanding = statements.findByUserIdAndStatusOrderByStatementMonthDesc(userId, "OPEN").stream()
                    .map(SimulationCardStatement::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (creditLimit.compareTo(outstanding) < 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "O limite não pode ser menor que as faturas em aberto.");
            }
        }
        card.setCreditLimit(creditLimit);
        cards.save(card);
        auditEvents.save(new AuditEvent(userId, "SIMULATION_CARD_CONFIGURED", userId));
        return get(userId);
    }

    @Transactional
    public CardView setBlocked(UUID userId, boolean blocked) {
        SimulationCard card = cards.findForUpdate(userId).orElseThrow(SimulationCardService::notFound);
        card.setBlocked(blocked);
        auditEvents.save(new AuditEvent(userId, blocked ? "SIMULATION_CARD_BLOCKED" : "SIMULATION_CARD_UNBLOCKED", userId));
        notifications.createOnce(userId, "card:block:" + blocked + ":" + java.time.LocalDate.now(),
                "CARD", blocked ? "Cartão simulado bloqueado." : "Cartão simulado desbloqueado.");
        return get(userId);
    }

    @Transactional
    public CardView reportIssue(UUID userId, String issue) {
        SimulationCard card = cards.findForUpdate(userId).orElseThrow(SimulationCardService::notFound);
        card.setBlocked(true);
        auditEvents.save(new AuditEvent(userId, "SIMULATION_CARD_ISSUE_REPORTED", userId));
        notifications.createOnce(userId, "card:report:" + issue + ":" + java.time.LocalDate.now(),
                "CARD", "Relato de " + issue + " recebido para o cartão simulado. Cartão bloqueado na simulação.");
        return get(userId);
    }

    @Transactional
    public CardTransactionView purchase(UUID userId, UUID requestId, String kind, BigDecimal amount, String description) {
        validateAmount(amount);
        if (requestId == null || description == null || description.isBlank() || description.trim().length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe uma referência e descrição fictícias.");
        }
        if (!"CREDIT".equals(kind) && !"DEBIT".equals(kind)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de compra inválido.");
        }
        SimulationCard card = cards.findForUpdate(userId).orElseThrow(SimulationCardService::notFound);
        var existing = transactions.findByUserIdAndRequestId(userId, requestId);
        if (existing.isPresent()) {
            SimulationCardTransaction previous = existing.get();
            if (!previous.getKind().equals(kind) || previous.getAmount().compareTo(amount) != 0
                    || !previous.getDescription().equals(description.trim())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Referência já usada para outra compra.");
            }
            return CardTransactionView.from(previous);
        }
        if (card.isBlocked()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Cartão simulado bloqueado.");
        amount = amount.setScale(2, RoundingMode.UNNECESSARY);
        String month = null;
        if ("CREDIT".equals(kind)) {
            BigDecimal outstanding = statements.findByUserIdAndStatusOrderByStatementMonthDesc(userId, "OPEN").stream()
                    .map(SimulationCardStatement::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (outstanding.add(amount).compareTo(card.getCreditLimit()) > 0) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Limite fictício insuficiente.");
            }
            month = YearMonth.now().toString();
            String statementMonth = month;
            SimulationCardStatement statement = statements.findForUpdate(userId, statementMonth)
                    .orElseGet(() -> statements.save(new SimulationCardStatement(userId, statementMonth)));
            statement.add(amount);
        } else {
            operations.operate(userId, requestId, amount, "CARD_DEBIT", description.trim());
        }
        SimulationCardTransaction saved = transactions.save(new SimulationCardTransaction(
                userId, requestId, kind, description.trim(), amount, month));
        auditEvents.save(new AuditEvent(userId, "SIMULATION_CARD_PURCHASE", saved.getId()));
        notifications.createOnce(userId, "card:purchase:" + saved.getId(), "CARD", "Compra fictícia registrada no cartão simulado.");
        return CardTransactionView.from(saved);
    }

    @Transactional
    public StatementView pay(UUID userId, String month, UUID requestId) {
        if (requestId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Referência de pagamento inválida.");
        SimulationCard card = cards.findForUpdate(userId).orElseThrow(SimulationCardService::notFound);
        SimulationCardStatement statement = statements.findForUpdate(userId, month)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fatura simulada não encontrada."));
        if ("PAID".equals(statement.getStatus())) {
            if (!requestId.equals(statement.getPaymentRequestId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Fatura simulada já paga.");
            }
            return StatementView.from(statement);
        }
        operations.operate(userId, requestId, statement.getAmount(), "CARD_BILL", "Fatura simulada " + month);
        statement.pay(requestId);
        return StatementView.from(statement);
    }

    @Transactional(readOnly = true)
    public List<StatementView> listStatements(UUID userId) {
        return statements.findTop24ByUserIdOrderByStatementMonthDesc(userId).stream().map(StatementView::from).toList();
    }

    @Transactional(readOnly = true)
    public List<CardTransactionView> transactions(UUID userId) {
        return transactions.findTop50ByUserIdOrderByCreatedAtDesc(userId).stream().map(CardTransactionView::from).toList();
    }

    private static void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0 || amount.scale() > 2 || amount.compareTo(MAX_LIMIT) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um valor positivo com até duas casas decimais.");
        }
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Cartão simulado não encontrado.");
    }

    public record CardView(boolean exists, BigDecimal creditLimit, Boolean blocked, BigDecimal availableLimit) {}
    public record StatementView(String month, BigDecimal amount, String status) {
        static StatementView from(SimulationCardStatement statement) {
            return new StatementView(statement.getStatementMonth(), statement.getAmount(), statement.getStatus());
        }
    }
    public record CardTransactionView(UUID id, String kind, String description, BigDecimal amount, String statementMonth,
                                      java.time.Instant createdAt, String category) {
        static CardTransactionView from(SimulationCardTransaction transaction) {
            return new CardTransactionView(transaction.getId(), transaction.getKind(), transaction.getDescription(),
                    transaction.getAmount(), transaction.getStatementMonth(), transaction.getCreatedAt(), transaction.getCategory());
        }
    }
}
