package com.giro_app.backend;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuditEventRepository auditEvents;

    public AuthController(AuthService authService, AuditEventRepository auditEvents) {
        this.authService = authService;
        this.auditEvents = auditEvents;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public AuthResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO response;
        try {
            response = authService.login(request);
        } catch (ResponseStatusException exception) {
            if (exception.getStatusCode().equals(HttpStatus.UNAUTHORIZED)
                    || exception.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
                auditEvents.save(new AuditEvent(null, "LOGIN_REJECTED", null));
            }
            throw exception;
        }
        auditEvents.save(new AuditEvent(response.user().id(), "LOGIN_SUCCEEDED", null));
        return response;
    }
}
