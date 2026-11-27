package com.giro_app.backend;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SplitGroupResponseDTO(
        UUID id,
        String name,
        UUID ownerUserId,
        List<MemberDTO> members,
        boolean canEdit
) {
    public static SplitGroupResponseDTO from(SplitGroup group) {
        return from(group, null);
    }

    public static SplitGroupResponseDTO from(SplitGroup group, UUID currentUserId) {
        List<MemberDTO> members = group.getMembers().stream()
                .map(member -> new MemberDTO(member.getId(), member.getUserId(), member.getPercentage()))
                .toList();
        return new SplitGroupResponseDTO(group.getId(), group.getName(), group.getOwnerUserId(), members,
                currentUserId != null && currentUserId.equals(group.getOwnerUserId()));
    }

    public record MemberDTO(UUID id, UUID userId, BigDecimal percentage) {
    }
}
