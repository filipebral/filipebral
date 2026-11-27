package com.giro_app.backend;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/business-applications")
public class BusinessApplicationController {
    private final BusinessApplicationService applications;

    public BusinessApplicationController(BusinessApplicationService applications) {
        this.applications = applications;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BusinessApplicationService.ApplicationView create(
            @RequestBody Request request, @AuthenticationPrincipal Jwt principal) {
        return applications.create(UUID.fromString(principal.getSubject()), request.cnpj());
    }

    @GetMapping("/current")
    public BusinessApplicationService.ApplicationView current(@AuthenticationPrincipal Jwt principal) {
        return applications.current(UUID.fromString(principal.getSubject()));
    }

    @PostMapping("/current/verification")
    public BusinessApplicationService.ApplicationView requestVerification(@AuthenticationPrincipal Jwt principal) {
        return applications.requestVerification(UUID.fromString(principal.getSubject()));
    }

    public record Request(String cnpj) {}
}
