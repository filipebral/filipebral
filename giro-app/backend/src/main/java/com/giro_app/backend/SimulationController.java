package com.giro_app.backend;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/simulation")
@ConditionalOnProperty(name = "app.simulation.enabled", havingValue = "true", matchIfMissing = true)
public class SimulationController {
    private final SimulationService service;
    private final SimulationScheduleService schedules;
    private final SimulationBillService bills;
    private final SimulationCardService cards;

    public SimulationController(SimulationService service, SimulationScheduleService schedules,
            SimulationBillService bills, SimulationCardService cards) {
        this.service = service;
        this.schedules = schedules;
        this.bills = bills;
        this.cards = cards;
    }

    @GetMapping("/status")
    public Status status() {
        return new Status(true);
    }

    @PostMapping("/account")
    public SimulationService.AccountView create(@AuthenticationPrincipal Jwt principal) {
        return service.create(userId(principal));
    }

    @GetMapping("/account")
    public SimulationService.AccountView account(@AuthenticationPrincipal Jwt principal) {
        return service.account(userId(principal));
    }

    @GetMapping("/statement")
    public List<SimulationService.TransactionView> statement(@AuthenticationPrincipal Jwt principal) {
        return service.statement(userId(principal));
    }

    @GetMapping("/history")
    public SimulationService.HistoryView history(@AuthenticationPrincipal Jwt principal,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) {
        return service.history(userId(principal), page, size);
    }

    @GetMapping("/operations/{requestId}")
    public SimulationService.TransactionView operation(@AuthenticationPrincipal Jwt principal, @PathVariable UUID requestId) {
        return service.operation(userId(principal), requestId);
    }

    @PostMapping("/credit")
    public SimulationService.AccountView credit(@AuthenticationPrincipal Jwt principal, @RequestBody Operation request) {
        return service.operate(userId(principal), request.requestId(), request.amount(), "CREDIT", null);
    }

    @PostMapping("/pix")
    public SimulationService.AccountView pix(@AuthenticationPrincipal Jwt principal, @RequestBody Operation request) {
        return service.operate(userId(principal), request.requestId(), request.amount(), "PIX", request.destination());
    }

    @PostMapping("/transfer")
    public SimulationService.AccountView transfer(@AuthenticationPrincipal Jwt principal, @RequestBody Operation request) {
        return service.operate(userId(principal), request.requestId(), request.amount(), "TRANSFER", request.destination());
    }

    @PostMapping("/schedules")
    public SimulationScheduleService.ScheduleView schedule(@AuthenticationPrincipal Jwt principal, @RequestBody ScheduleRequest request) {
        return schedules.create(userId(principal), request.requestId(), request.amount(), request.destination(), request.scheduledAt());
    }

    @GetMapping("/schedules")
    public List<SimulationScheduleService.ScheduleView> schedules(@AuthenticationPrincipal Jwt principal) {
        return schedules.list(userId(principal));
    }

    @PostMapping("/schedules/{id}/cancel")
    public SimulationScheduleService.ScheduleView cancel(@AuthenticationPrincipal Jwt principal, @PathVariable UUID id) {
        return schedules.cancel(userId(principal), id);
    }

    @PostMapping("/bills")
    public SimulationBillService.BillView createBill(@AuthenticationPrincipal Jwt principal, @RequestBody BillRequest request) {
        return bills.create(userId(principal), request.requestId(), request.description(), request.amount(), request.dueDate());
    }

    @GetMapping("/bills")
    public List<SimulationBillService.BillView> bills(@AuthenticationPrincipal Jwt principal) {
        return bills.list(userId(principal));
    }

    @PostMapping("/bills/{id}/pay")
    public SimulationBillService.BillView payBill(@AuthenticationPrincipal Jwt principal, @PathVariable UUID id) {
        return bills.pay(userId(principal), id);
    }

    private UUID userId(Jwt principal) {
        return UUID.fromString(principal.getSubject());
    }

    public record Operation(UUID requestId, BigDecimal amount, String destination) {}
    public record ScheduleRequest(UUID requestId, BigDecimal amount, String destination, Instant scheduledAt) {}
    public record BillRequest(UUID requestId, String description, BigDecimal amount, LocalDate dueDate) {}
    public record Status(boolean simulated) {}

    @GetMapping("/card")
    public SimulationCardService.CardView card(@AuthenticationPrincipal Jwt principal) {
        return cards.get(userId(principal));
    }

    @PostMapping("/card")
    public SimulationCardService.CardView issueCard(@AuthenticationPrincipal Jwt principal, @RequestBody CardSetup request) {
        return cards.issue(userId(principal), request.creditLimit());
    }

    @PostMapping("/card/block")
    public SimulationCardService.CardView blockCard(@AuthenticationPrincipal Jwt principal) {
        return cards.setBlocked(userId(principal), true);
    }

    @PostMapping("/card/unblock")
    public SimulationCardService.CardView unblockCard(@AuthenticationPrincipal Jwt principal) {
        return cards.setBlocked(userId(principal), false);
    }

    @PostMapping("/card/report-loss")
    public SimulationCardService.CardView reportCardLoss(@AuthenticationPrincipal Jwt principal) {
        return cards.reportIssue(userId(principal), "perda");
    }

    @PostMapping("/card/report-fraud")
    public SimulationCardService.CardView reportCardFraud(@AuthenticationPrincipal Jwt principal) {
        return cards.reportIssue(userId(principal), "suspeita de fraude");
    }

    @PostMapping("/card/purchases")
    public SimulationCardService.CardTransactionView cardPurchase(
            @AuthenticationPrincipal Jwt principal, @RequestBody CardPurchase request) {
        return cards.purchase(userId(principal), request.requestId(), request.kind(), request.amount(), request.description());
    }

    @GetMapping("/card/transactions")
    public List<SimulationCardService.CardTransactionView> cardTransactions(@AuthenticationPrincipal Jwt principal) {
        return cards.transactions(userId(principal));
    }

    @GetMapping("/card/statements")
    public List<SimulationCardService.StatementView> cardStatements(@AuthenticationPrincipal Jwt principal) {
        return cards.listStatements(userId(principal));
    }

    @PostMapping("/card/statements/{month}/pay")
    public SimulationCardService.StatementView payCardStatement(
            @AuthenticationPrincipal Jwt principal, @PathVariable String month, @RequestBody PayStatement request) {
        return cards.pay(userId(principal), month, request.requestId());
    }

    public record CardSetup(BigDecimal creditLimit) {}
    public record CardPurchase(UUID requestId, String kind, BigDecimal amount, String description) {}
    public record PayStatement(UUID requestId) {}
}
