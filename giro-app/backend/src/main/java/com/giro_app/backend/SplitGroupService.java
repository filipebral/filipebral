package com.giro_app.backend;

import java.util.List;
import java.util.UUID;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SplitGroupService {
    private final SplitGroupRepository groups;
    private final SplitTransactionRepository transactions;
    private final SplitPaymentShareRepository shares;

    public SplitGroupService(SplitGroupRepository groups, SplitTransactionRepository transactions,
                             SplitPaymentShareRepository shares) {
        this.groups = groups;
        this.transactions = transactions;
        this.shares = shares;
    }

    @Transactional(readOnly = true)
    public List<SplitGroupResponseDTO> groupsVisibleTo(UUID userId) {
        return groups.findVisibleTo(userId).stream().map(group -> SplitGroupResponseDTO.from(group, userId)).toList();
    }

    @Transactional
    public SplitGroupResponseDTO update(UUID userId, UUID groupId, CreateSplitGroupRequestDTO request) {
        SplitGroup group = groups.findById(groupId).orElseThrow(SplitGroupService::groupNotFound);
        requireOwner(group, userId);
        group.update(request.name(), inputs(request));
        return SplitGroupResponseDTO.from(group, userId);
    }

    @Transactional(readOnly = true)
    public List<HistoryView> history(UUID userId, UUID groupId) {
        SplitGroup group = groups.findById(groupId).orElseThrow(SplitGroupService::groupNotFound);
        if (!isVisibleTo(group, userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não pode consultar este rateio.");
        return transactions.findBySplitGroupId(groupId).stream()
                .map(transaction -> new HistoryView(transaction.getId(), transaction.getDescription(),
                        transaction.getTotalAmount(), transaction.getCreatedAt(),
                        shares.findBySplitTransactionIdOrderByMemberUserId(transaction.getId()).stream()
                                .map(ShareView::from).toList()))
                .toList();
    }

    @Transactional
    public ShareView reportPaid(UUID userId, UUID transactionId, UUID memberUserId) {
        SplitTransactionEntity transaction = transactions.findById(transactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rateio não encontrado."));
        SplitGroup group = groups.findById(transaction.getSplitGroupId()).orElseThrow(SplitGroupService::groupNotFound);
        if (!isVisibleTo(group, userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não pode atualizar este rateio.");
        if (!Objects.equals(group.getOwnerUserId(), userId) && !memberUserId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Participantes só podem informar o pagamento da própria cota.");
        }
        SplitPaymentShare share = shares.findBySplitTransactionIdAndMemberUserId(transactionId, memberUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cota do participante não encontrada."));
        share.markPaid(userId);
        return ShareView.from(share);
    }

    @Transactional
    public ShareView reportOwnPaid(UUID userId, UUID transactionId) {
        return reportPaid(userId, transactionId, userId);
    }

    static List<SplitGroup.GroupMemberInput> inputs(CreateSplitGroupRequestDTO request) {
        if (request == null || request.members() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe os participantes do grupo.");
        }
        return request.members().stream().map(member ->
                new SplitGroup.GroupMemberInput(member.userId(), member.percentage())).toList();
    }

    private static boolean isVisibleTo(SplitGroup group, UUID userId) {
        return Objects.equals(group.getOwnerUserId(), userId)
                || group.getMembers().stream().anyMatch(member -> member.getUserId().equals(userId));
    }

    private static void requireOwner(SplitGroup group, UUID userId) {
        if (!Objects.equals(group.getOwnerUserId(), userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Somente o dono pode editar este grupo.");
    }

    private static ResponseStatusException groupNotFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Grupo de rateio não encontrado.");
    }

    public record ShareView(UUID memberUserId, java.math.BigDecimal allocatedAmount, String status,
                            java.time.Instant paidAt, UUID reportedBy) {
        static ShareView from(SplitPaymentShare share) {
            return new ShareView(share.getMemberUserId(), share.getAllocatedAmount(), share.getStatus(),
                    share.getPaidAt(), share.getPaidBy());
        }
    }
    public record HistoryView(UUID transactionId, String description, java.math.BigDecimal totalAmount,
                              java.time.LocalDateTime createdAt, List<ShareView> shares) {}
}
