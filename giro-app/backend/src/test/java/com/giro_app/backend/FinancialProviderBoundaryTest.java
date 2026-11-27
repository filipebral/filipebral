package com.giro_app.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class FinancialProviderBoundaryTest {
    @Test
    void accountOpeningStaysUnavailableEvenWhenLegacyFlagIsEnabled() {
        new ApplicationContextRunner()
                .withPropertyValues("baas.enabled=true")
                .withUserConfiguration(AccountController.class)
                .run(context -> {
                    AccountController controller = context.getBean(AccountController.class);
                    ResponseStatusException error = assertThrows(ResponseStatusException.class,
                            () -> controller.createAccount(new AccountRequestDTO("customer"), null));
                    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getStatusCode());
                });
    }

    @Test
    void pixRequestRejectsInvalidAmountsAndMissingKey() {
        UUID account = UUID.randomUUID();
        UUID request = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () -> new FinancialProviderPort.PixRequest(
                account, request, BigDecimal.ZERO, FinancialProviderPort.PixKeyType.RANDOM, "key"));
        assertThrows(IllegalArgumentException.class, () -> new FinancialProviderPort.PixRequest(
                account, request, new BigDecimal("1.001"), FinancialProviderPort.PixKeyType.RANDOM, "key"));
        assertThrows(IllegalArgumentException.class, () -> new FinancialProviderPort.PixRequest(
                account, request, BigDecimal.ONE, FinancialProviderPort.PixKeyType.RANDOM, " "));
    }

    @Test
    void pixRequestDoesNotExposeKeyInToString() {
        var request = new FinancialProviderPort.PixRequest(UUID.randomUUID(), UUID.randomUUID(),
                BigDecimal.ONE, FinancialProviderPort.PixKeyType.RANDOM, "sensitive-key");
        assertFalse(request.toString().contains("sensitive-key"));
    }
}
