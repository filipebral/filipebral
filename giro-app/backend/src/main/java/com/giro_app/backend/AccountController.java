package com.giro_app.backend;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    @PostMapping
    public ResponseEntity<Account> createAccount(
            @Valid @RequestBody AccountRequestDTO request,
            @AuthenticationPrincipal Jwt principal
    ) {
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "A criação de contas está desabilitada até a integração com um BaaS real.");
    }
}