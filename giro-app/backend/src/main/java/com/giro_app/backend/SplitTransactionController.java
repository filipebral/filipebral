package com.giro_app.backend;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/split-transactions")
public class SplitTransactionController {

    private final ProcessSplitTransactionUseCase processSplitTransactionUseCase;
    private final SplitGroupService splitGroupService;

    public SplitTransactionController(ProcessSplitTransactionUseCase processSplitTransactionUseCase, SplitGroupService splitGroupService) {
        this.processSplitTransactionUseCase = processSplitTransactionUseCase;
        this.splitGroupService = splitGroupService;
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

    @PatchMapping("/{transactionId}/shares/{memberId}/report-paid")
    public SplitGroupService.ShareView reportPaid(@PathVariable UUID transactionId, @PathVariable UUID memberId,
            @AuthenticationPrincipal Jwt principal) {
        return splitGroupService.reportPaid(UUID.fromString(principal.getSubject()), transactionId, memberId);
    }

    @PatchMapping("/{transactionId}/report-my-payment")
    public SplitGroupService.ShareView reportOwnPaid(@PathVariable UUID transactionId,
            @AuthenticationPrincipal Jwt principal) {
        return splitGroupService.reportOwnPaid(UUID.fromString(principal.getSubject()), transactionId);
    }
}