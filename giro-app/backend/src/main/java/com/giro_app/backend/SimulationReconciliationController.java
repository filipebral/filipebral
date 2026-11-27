package com.giro_app.backend;

import java.util.List;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/simulation/reconciliation")
@ConditionalOnProperty(name = "app.simulation.enabled", havingValue = "true", matchIfMissing = true)
public class SimulationReconciliationController {
    private final SimulationReconciliationService service;

    SimulationReconciliationController(SimulationReconciliationService service) {
        this.service = service;
    }

    @PostMapping
    public SimulationReconciliationService.View register(@AuthenticationPrincipal Jwt principal, @RequestBody Request request) {
        return service.register(UUID.fromString(principal.getSubject()), request.requestId());
    }

    @GetMapping
    public List<SimulationReconciliationService.View> list(@AuthenticationPrincipal Jwt principal) {
        return service.list(UUID.fromString(principal.getSubject()));
    }

    @PostMapping("/{id}/stop")
    public SimulationReconciliationService.View stop(@AuthenticationPrincipal Jwt principal, @PathVariable UUID id) {
        return service.stop(UUID.fromString(principal.getSubject()), id);
    }

    @PostMapping("/{id}/check")
    public SimulationReconciliationService.View check(@AuthenticationPrincipal Jwt principal, @PathVariable UUID id) {
        return service.checkForUser(UUID.fromString(principal.getSubject()), id);
    }

    record Request(UUID requestId) {}
}
