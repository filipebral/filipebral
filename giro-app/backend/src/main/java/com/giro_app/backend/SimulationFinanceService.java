package com.giro_app.backend;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@ConditionalOnProperty(name = "app.simulation.enabled", havingValue = "true", matchIfMissing = true)
public class SimulationFinanceService {
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("999999999.99");
    private final SimulationAccountRepository accounts;
    private final SimulationTransactionRepository transactions;
    private final SimulationCardTransactionRepository cardTransactions;
    private final SimulationBudgetRepository budgets;
    private final SimulationGoalRepository goals;
    private final SimulationGoalMovementRepository movements;
    private final InAppNotificationService notifications;
    private final AuditEventRepository auditEvents;

    public SimulationFinanceService(SimulationAccountRepository accounts, SimulationTransactionRepository transactions,
            SimulationCardTransactionRepository cardTransactions, SimulationBudgetRepository budgets,
            SimulationGoalRepository goals, SimulationGoalMovementRepository movements,
            InAppNotificationService notifications, AuditEventRepository auditEvents) {
        this.accounts = accounts;
        this.transactions = transactions;
        this.cardTransactions = cardTransactions;
        this.budgets = budgets;
        this.goals = goals;
        this.movements = movements;
        this.notifications = notifications;
        this.auditEvents = auditEvents;
    }

    public List<FinancialCategory.CategoryView> categories() {
        return FinancialCategory.views();
    }

    @Transactional
    public void categorizeTransaction(UUID userId, UUID transactionId, String categoryValue) {
        SimulationTransaction transaction = transactions.findByUserIdAndId(userId, transactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movimentação simulada não encontrada."));
        if ("CREDIT".equals(transaction.getType()) || "CARD_BILL".equals(transaction.getType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Esta movimentação não pode ser classificada como despesa.");
        }
        transaction.setCategory(FinancialCategory.parse(categoryValue).name());
        checkBudget(userId, transaction.getCategory(), YearMonth.from(transaction.getCreatedAt().atZone(ZoneOffset.UTC)));
    }

    @Transactional
    public void categorizeCardTransaction(UUID userId, UUID transactionId, String categoryValue) {
        SimulationCardTransaction transaction = cardTransactions.findByUserIdAndId(userId, transactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compra simulada não encontrada."));
        transaction.setCategory(FinancialCategory.parse(categoryValue).name());
        if ("CREDIT".equals(transaction.getKind())) {
            checkBudget(userId, transaction.getCategory(), YearMonth.from(transaction.getCreatedAt().atZone(ZoneOffset.UTC)));
        }
    }

    @Transactional
    public BudgetView setBudget(UUID userId, String monthValue, String categoryValue, BigDecimal limit) {
        YearMonth month = parseMonth(monthValue);
        FinancialCategory category = FinancialCategory.parse(categoryValue);
        validateAmount(limit);
        accounts.findById(userId).orElseThrow(SimulationFinanceService::accountNotFound);
        SimulationBudget budget = budgets.findByUserIdAndMonthAndCategory(userId, month.toString(), category.name())
                .orElseGet(() -> new SimulationBudget(userId, month.toString(), category.name(), limit.setScale(2, RoundingMode.UNNECESSARY)));
        budget.setLimitAmount(limit.setScale(2, RoundingMode.UNNECESSARY));
        budgets.save(budget);
        BudgetView result = budgetView(userId, budget, month);
        notifyBudget(userId, result);
        return result;
    }

    @Transactional(readOnly = true)
    public List<BudgetView> budgets(UUID userId, String monthValue) {
        YearMonth month = parseMonth(monthValue);
        return budgets.findByUserIdAndMonthOrderByCategory(userId, month.toString()).stream()
                .map(budget -> budgetView(userId, budget, month)).toList();
    }

    @Transactional
    public GoalView createGoal(UUID userId, String name, BigDecimal target) {
        validateAmount(target);
        if (name == null || name.isBlank() || name.trim().length() > 80) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um nome de meta com até 80 caracteres.");
        }
        SimulationAccount account = accounts.findForUpdate(userId).orElseThrow(SimulationFinanceService::accountNotFound);
        SimulationGoal goal = goals.save(new SimulationGoal(userId, name.trim(), target.setScale(2, RoundingMode.UNNECESSARY)));
        auditEvents.save(new AuditEvent(userId, "SIMULATION_GOAL_CREATED", goal.getId()));
        return GoalView.from(goal, account);
    }

    @Transactional(readOnly = true)
    public List<GoalView> goals(UUID userId) {
        SimulationAccount account = accounts.findById(userId).orElseThrow(SimulationFinanceService::accountNotFound);
        return goals.findByUserIdOrderByName(userId).stream().map(goal -> GoalView.from(goal, account)).toList();
    }

    @Transactional
    public GoalView contribute(UUID userId, UUID goalId, UUID requestId, BigDecimal amount) {
        return moveGoal(userId, goalId, requestId, amount, "RESERVE");
    }

    @Transactional
    public GoalView release(UUID userId, UUID goalId, UUID requestId, BigDecimal amount) {
        return moveGoal(userId, goalId, requestId, amount, "RELEASE");
    }

    private GoalView moveGoal(UUID userId, UUID goalId, UUID requestId, BigDecimal amount, String type) {
        validateAmount(amount);
        if (requestId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe uma referência única para a operação.");
        SimulationAccount account = accounts.findForUpdate(userId).orElseThrow(SimulationFinanceService::accountNotFound);
        SimulationGoal goal = goals.findForUpdate(goalId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meta não encontrada."));
        var existing = movements.findByUserIdAndRequestId(userId, requestId);
        if (existing.isPresent()) {
            SimulationGoalMovement previous = existing.get();
            if (!previous.getGoalId().equals(goalId) || !previous.getType().equals(type)
                    || previous.getAmount().compareTo(amount) != 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Referência já usada para outra movimentação.");
            }
            return GoalView.from(goal, account);
        }
        amount = amount.setScale(2, RoundingMode.UNNECESSARY);
        if ("RESERVE".equals(type)) {
            if (account.getAvailableBalance().compareTo(amount) < 0) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Saldo fictício disponível insuficiente.");
            }
            if (goal.getSavedAmount().add(amount).compareTo(goal.getTargetAmount()) > 0) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "O aporte ultrapassaria o valor da meta.");
            }
            account.setReservedBalance(account.getReservedBalance().add(amount));
            goal.add(amount);
            notifications.createOnce(userId, "goal:" + requestId, "GOAL", "Aporte fictício reservado para a meta " + goal.getName() + ".");
        } else {
            if (goal.getSavedAmount().compareTo(amount) < 0) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "A meta não possui esse valor reservado.");
            }
            account.setReservedBalance(account.getReservedBalance().subtract(amount));
            goal.release(amount);
            notifications.createOnce(userId, "goal:" + requestId, "GOAL", "Valor fictício liberado da meta " + goal.getName() + ".");
        }
        movements.save(new SimulationGoalMovement(userId, goalId, requestId, type, amount));
        auditEvents.save(new AuditEvent(userId, "SIMULATION_GOAL_" + type, goalId));
        return GoalView.from(goal, account);
    }

    private BudgetView budgetView(UUID userId, SimulationBudget budget, YearMonth month) {
        Instant start = month.atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = month.plusMonths(1).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        BigDecimal spent = transactions.sumExpenses(userId, budget.getCategory(), start, end)
                .add(cardTransactions.sumCreditPurchases(userId, budget.getCategory(), start, end));
        return new BudgetView(month.toString(), budget.getCategory(), FinancialCategory.parse(budget.getCategory()).label(),
                budget.getLimitAmount(), spent, spent.multiply(new BigDecimal("100"))
                .divide(budget.getLimitAmount(), 2, RoundingMode.HALF_UP));
    }

    private void checkBudget(UUID userId, String category, YearMonth month) {
        budgets.findByUserIdAndMonthAndCategory(userId, month.toString(), category).ifPresent(budget -> {
            BudgetView view = budgetView(userId, budget, month);
            notifyBudget(userId, view);
        });
    }

    private void notifyBudget(UUID userId, BudgetView budget) {
        if (budget.spent().compareTo(budget.limitAmount()) >= 0) {
            notifications.createOnce(userId, "budget:" + budget.month() + ":" + budget.category() + ":100",
                    "BUDGET", "O orçamento simulado de " + budget.label() + " atingiu o limite.");
        } else if (budget.percent().compareTo(new BigDecimal("80")) >= 0) {
            notifications.createOnce(userId, "budget:" + budget.month() + ":" + budget.category() + ":80",
                    "BUDGET", "O orçamento simulado de " + budget.label() + " chegou a pelo menos 80%.");
        }
    }

    private static YearMonth parseMonth(String value) {
        try {
            return YearMonth.parse(value);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o mês no formato AAAA-MM.");
        }
    }

    private static void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0 || amount.scale() > 2 || amount.compareTo(MAX_AMOUNT) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um valor positivo com até duas casas decimais.");
        }
    }

    private static ResponseStatusException accountNotFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta simulada não encontrada.");
    }

    public record BudgetView(String month, String category, String label, BigDecimal limitAmount, BigDecimal spent, BigDecimal percent) {}
    public record GoalView(UUID id, String name, BigDecimal targetAmount, BigDecimal savedAmount,
                           BigDecimal reservedBalance, BigDecimal availableBalance) {
        static GoalView from(SimulationGoal goal, SimulationAccount account) {
            return new GoalView(goal.getId(), goal.getName(), goal.getTargetAmount(), goal.getSavedAmount(),
                    account.getReservedBalance(), account.getAvailableBalance());
        }
    }
}
