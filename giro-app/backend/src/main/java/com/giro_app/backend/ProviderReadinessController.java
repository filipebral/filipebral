package com.giro_app.backend;

import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/simulation/providers")
@ConditionalOnProperty(name = "app.simulation.enabled", havingValue = "true", matchIfMissing = true)
public class ProviderReadinessController {
    private final ObjectProvider<FinancialProviderPort> financial;
    private final ObjectProvider<CardProviderPort> cards;
    private final ObjectProvider<CreditProviderPort> credit;
    private final ObjectProvider<InvestmentProviderPort> investments;
    private final ObjectProvider<InsuranceProviderPort> insurance;
    private final ObjectProvider<NotificationDeliveryPort> notifications;

    public ProviderReadinessController(ObjectProvider<FinancialProviderPort> financial,
            ObjectProvider<CardProviderPort> cards, ObjectProvider<CreditProviderPort> credit,
            ObjectProvider<InvestmentProviderPort> investments, ObjectProvider<InsuranceProviderPort> insurance,
            ObjectProvider<NotificationDeliveryPort> notifications) {
        this.financial = financial;
        this.cards = cards;
        this.credit = credit;
        this.investments = investments;
        this.insurance = insurance;
        this.notifications = notifications;
    }

    @GetMapping("/readiness")
    public Readiness readiness() {
        return new Readiness(status(financial), status(cards), status(credit), status(investments), status(insurance),
                status(notifications), List.of("EMAIL", "PUSH", "SMS"));
    }

    private String status(ObjectProvider<?> provider) {
        return provider.getIfAvailable() == null ? "NOT_CONFIGURED" : "ADAPTER_REGISTERED";
    }

    public record Readiness(String accountAndPayments, String cards, String credit, String investments,
                            String insurance, String notifications, List<String> notificationChannels) {}
}
