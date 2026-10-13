package com.giro_app.backend;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProcessSplitTransactionUseCase {

    private final SplitGroupRepository splitGroupRepository;
    private final SplitTransactionRepository splitTransactionRepository;

    public ProcessSplitTransactionUseCase(
            SplitGroupRepository splitGroupRepository,
            SplitTransactionRepository splitTransactionRepository
    ) {
        this.splitGroupRepository = splitGroupRepository;
        this.splitTransactionRepository = splitTransactionRepository;
    }

    @Transactional
    public SplitTransactionResponseDTO execute(SplitTransactionRequestDTO request, UUID requesterId) {
        SplitGroup group = splitGroupRepository.findById(request.splitGroupId())
                .orElseThrow(() -> new IllegalArgumentException("Grupo de rateio não encontrado com o ID: " + request.splitGroupId()));
        if (!requesterId.equals(group.getOwnerUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não pode processar este grupo de rateio.");
        }

        UUID transactionId = UUID.randomUUID();

        // 1. Calcula as cotas de cada membro
        List<SplitTransactionResponseDTO.MemberShareDTO> shares = group.getMembers().stream().map(member -> {
            BigDecimal allocatedAmount = request.totalAmount()
                    .multiply(member.getPercentage())
                    .divide(new BigDecimal("100.00"), 2, RoundingMode.HALF_EVEN);

            return new SplitTransactionResponseDTO.MemberShareDTO(
                    member.getUserId(),
                    member.getPercentage(),
                    allocatedAmount
            );
        }).collect(Collectors.toList());

        // 2. Persiste o histórico da transação no banco de dados
        SplitTransactionEntity entity = new SplitTransactionEntity(
                transactionId,
                group.getId(),
                request.description(),
                request.totalAmount()
        );
        splitTransactionRepository.save(entity);

        System.out.println(">> [SPLIT ENGINE] Transação " + transactionId + " salva com sucesso no banco!");

        return new SplitTransactionResponseDTO(
                transactionId,
                request.description(),
                request.totalAmount(),
                shares
        );
    }
}