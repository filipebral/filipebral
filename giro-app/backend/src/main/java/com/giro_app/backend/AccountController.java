package com.giro_app.backend;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final CreateAccountUseCase createAccountUseCase;
    private final boolean baasEnabled;

    public AccountController(
            CreateAccountUseCase createAccountUseCase,
            @Value("${baas.enabled:false}") boolean baasEnabled
    ) {
        this.createAccountUseCase = createAccountUseCase;
        this.baasEnabled = baasEnabled;
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(
            @Valid @RequestBody AccountRequestDTO request,
            @AuthenticationPrincipal Jwt principal
    ) {
        if (!baasEnabled) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "A criação de contas está desabilitada até a integração com um BaaS real.");
        }

        // Converte o DTO da web para o Comando interno do domínio
        CreateAccountCommand command = new CreateAccountCommand(
                UUID.fromString(principal.getSubject()),
                request.baasCustomerId());
        
        // Executa a orquestração
        Account createdAccount = createAccountUseCase.execute(command);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
    }
}