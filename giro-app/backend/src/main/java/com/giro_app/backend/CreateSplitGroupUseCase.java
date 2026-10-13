package com.giro_app.backend;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CreateSplitGroupUseCase {

    private final SplitGroupRepository splitGroupRepository;

    public CreateSplitGroupUseCase(SplitGroupRepository splitGroupRepository) {
        this.splitGroupRepository = splitGroupRepository;
    }

    @Transactional
    public SplitGroup execute(CreateSplitGroupRequestDTO request, UUID ownerUserId) {
        var memberInputs = request.members().stream()
                .map(m -> new SplitGroup.GroupMemberInput(m.userId(), m.percentage()))
                .collect(Collectors.toList());

        SplitGroup group = new SplitGroup(ownerUserId, request.name(), memberInputs);
        return splitGroupRepository.save(group);
    }
}
