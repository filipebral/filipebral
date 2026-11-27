package com.giro_app.backend;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/simulation/finance")
@ConditionalOnProperty(name = "app.simulation.enabled", havingValue = "true", matchIfMissing = true)
public class SimulationFinanceController {
    private final SimulationFinanceService finance;

    public SimulationFinanceController(SimulationFinanceService finance) { this.finance = finance; }

    @GetMapping("/categories")
    public List<FinancialCategory.CategoryView> categories() { return finance.categories(); }

    @PatchMapping("/transactions/{id}/category")
    public void categorizeTransaction(@AuthenticationPrincipal Jwt principal, @PathVariable UUID id,
            @RequestBody CategoryRequest request) {
        finance.categorizeTransaction(userId(principal), id, request.category());
    }

    @PatchMapping("/card-transactions/{id}/category")
    public void categorizeCardTransaction(@AuthenticationPrincipal Jwt principal, @PathVariable UUID id,
            @RequestBody CategoryRequest request) {
        finance.categorizeCardTransaction(userId(principal), id, request.category());
    }

    @GetMapping("/budgets")
    public List<SimulationFinanceService.BudgetView> budgets(@AuthenticationPrincipal Jwt principal,
            @RequestParam(defaultValue = "") String month) {
        return finance.budgets(userId(principal), month.isBlank() ? YearMonth.now().toString() : month);
    }

    @PutMapping("/budgets")
    public SimulationFinanceService.BudgetView setBudget(@AuthenticationPrincipal Jwt principal, @RequestBody BudgetRequest request) {
        return finance.setBudget(userId(principal), request.month(), request.category(), request.limitAmount());
    }

    @GetMapping("/goals")
    public List<SimulationFinanceService.GoalView> goals(@AuthenticationPrincipal Jwt principal) {
        return finance.goals(userId(principal));
    }

    @PostMapping("/goals")
    public SimulationFinanceService.GoalView createGoal(@AuthenticationPrincipal Jwt principal, @RequestBody GoalRequest request) {
        return finance.createGoal(userId(principal), request.name(), request.targetAmount());
    }

    @PostMapping("/goals/{id}/contributions")
    public SimulationFinanceService.GoalView contribute(@AuthenticationPrincipal Jwt principal, @PathVariable UUID id,
            @RequestBody GoalMovementRequest request) {
        return finance.contribute(userId(principal), id, request.requestId(), request.amount());
    }

    @PostMapping("/goals/{id}/releases")
    public SimulationFinanceService.GoalView release(@AuthenticationPrincipal Jwt principal, @PathVariable UUID id,
            @RequestBody GoalMovementRequest request) {
        return finance.release(userId(principal), id, request.requestId(), request.amount());
    }

    private UUID userId(Jwt principal) { return UUID.fromString(principal.getSubject()); }
    public record CategoryRequest(String category) {}
    public record BudgetRequest(String month, String category, BigDecimal limitAmount) {}
    public record GoalRequest(String name, BigDecimal targetAmount) {}
    public record GoalMovementRequest(UUID requestId, BigDecimal amount) {}
}
