package com.giro_app.backend;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/split-transactions")
public class SplitTransactionController {

    private final ProcessSplitTransactionUseCase processSplitTransactionUseCase;

    public SplitTransactionController(ProcessSplitTransactionUseCase processSplitTransactionUseCase) {
        this.processSplitTransactionUseCase = processSplitTransactionUseCase;
    }

    @PostMapping
    public ResponseEntity<SplitTransactionResponseDTO> processSplit(
            @RequestBody SplitTransactionRequestDTO request,
            @AuthenticationPrincipal Jwt principal
    ) {
        SplitTransactionResponseDTO response = processSplitTransactionUseCase.execute(
                request,
                UUID.fromString(principal.getSubject()));
        return ResponseEntity.ok(response);
    }
}