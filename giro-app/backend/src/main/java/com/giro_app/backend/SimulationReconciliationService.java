package com.giro_app.backend;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@ConditionalOnProperty(name = "app.simulation.enabled", havingValue = "true", matchIfMissing = true)
public class SimulationReconciliationService {
    private final SimulationAccountRepository accounts;
    private final SimulationTransactionRepository transactions;
    private final SimulationReconciliationRepository queue;

    SimulationReconciliationService(SimulationAccountRepository accounts,
            SimulationTransactionRepository transactions, SimulationReconciliationRepository queue) {
        this.accounts = accounts;
        this.transactions = transactions;
        this.queue = queue;
    }

    @Transactional
    public View register(UUID userId, UUID requestId) {
        if (requestId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a referência da operação simulada.");
        }
        accounts.findForUpdate(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta simulada não encontrada."));
        var existing = queue.findByUserIdAndRequestId(userId, requestId);
        if (existing.isPresent()) {
            SimulationReconciliation item = queue.findForUpdate(existing.get().getId()).orElseThrow();
            if ("STOPPED".equals(item.getStatus())) {
                ensureCapacity(userId);
                item.resume();
            }
            return view(item);
        }
        ensureCapacity(userId);
        return view(queue.save(new SimulationReconciliation(userId, requestId)));
    }

    @Transactional(readOnly = true)
    public List<View> list(UUID userId) {
        return queue.findForUser(userId, PageRequest.of(0, 100)).stream().map(this::view).toList();
    }

    @Transactional
    public View stop(UUID userId, UUID id) {
        SimulationReconciliation item = queue.findForUpdate(id).filter(entry -> entry.getUserId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Consulta simulada não encontrada."));
        if ("UNKNOWN".equals(item.getStatus())) item.stop();
        return view(item);
    }

    @Transactional
    public void check(UUID id) {
        SimulationReconciliation item = queue.findForUpdate(id).orElseThrow();
        checkItem(item);
    }

    @Transactional
    public View checkForUser(UUID userId, UUID id) {
        SimulationReconciliation item = queue.findForUpdate(id).filter(entry -> entry.getUserId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Consulta simulada não encontrada."));
        checkItem(item);
        return view(item);
    }

    private void checkItem(SimulationReconciliation item) {
        if (!"UNKNOWN".equals(item.getStatus())) return;
        transactions.findByUserIdAndRequestId(item.getUserId(), item.getRequestId())
                .ifPresentOrElse(transaction -> item.complete(transaction.getId()),
                        () -> item.postpone(Instant.now().plusSeconds(300)));
    }

    private void ensureCapacity(UUID userId) {
        if (queue.countByUserIdAndStatus(userId, "UNKNOWN") >= 50) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Limite de consultas simuladas pendentes atingido.");
        }
    }

    private View view(SimulationReconciliation item) {
        return new View(item.getId(), item.getRequestId(), item.getStatus(), item.getTransactionId(), item.getCreatedAt());
    }

    public record View(UUID id, UUID requestId, String status, UUID transactionId, Instant createdAt) {}
}
