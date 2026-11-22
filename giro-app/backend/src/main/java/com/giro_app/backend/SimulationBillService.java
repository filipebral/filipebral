package com.giro_app.backend;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@ConditionalOnProperty(name = "app.simulation.enabled", havingValue = "true", matchIfMissing = true)
public class SimulationBillService {
    private final SimulationAccountRepository accounts;
    private final SimulationBillRepository bills;
    private final SimulationService operations;

    public SimulationBillService(SimulationAccountRepository accounts, SimulationBillRepository bills, SimulationService operations) {
        this.accounts = accounts;
        this.bills = bills;
        this.operations = operations;
    }

    @Transactional
    public BillView create(UUID userId, UUID requestId, String description, BigDecimal amount, LocalDate dueDate) {
        String name = SimulationService.validateOperation(requestId, amount, "BILL", description);
        if (dueDate == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o vencimento fictício.");
        accounts.findForUpdate(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta simulada não encontrada."));
        var existing = bills.findByUserIdAndRequestId(userId, requestId);
        if (existing.isPresent()) {
            SimulationBill bill = existing.get();
            if (!bill.getDescription().equals(name) || bill.getAmount().compareTo(amount) != 0 || !bill.getDueDate().equals(dueDate)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Identificador já usado para outro boleto.");
            }
            return BillView.from(bill);
        }
        return BillView.from(bills.save(new SimulationBill(userId, requestId, name, amount, dueDate)));
    }

    @Transactional(readOnly = true)
    public List<BillView> list(UUID userId) {
        return bills.findTop50ByUserIdOrderByDueDateDescIdDesc(userId).stream().map(BillView::from).toList();
    }

    @Transactional
    public BillView pay(UUID userId, UUID id) {
        SimulationBill bill = bills.findForUpdate(id).filter(item -> item.getUserId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Boleto simulado não encontrado."));
        if ("PAID".equals(bill.getStatus())) return BillView.from(bill);
        operations.operate(userId, bill.getExecutionId(), bill.getAmount(), "BILL", bill.getDescription());
        bill.setStatus("PAID");
        return BillView.from(bill);
    }

    public record BillView(UUID id, String description, BigDecimal amount, LocalDate dueDate, String status) {
        static BillView from(SimulationBill bill) {
            return new BillView(bill.getId(), bill.getDescription(), bill.getAmount(), bill.getDueDate(), bill.getStatus());
        }
    }
}
