package com.giro_app.backend;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/split-groups")
public class SplitGroupController {

    private final CreateSplitGroupUseCase createSplitGroupUseCase;
    private final SplitGroupService splitGroupService;

    public SplitGroupController(CreateSplitGroupUseCase createSplitGroupUseCase, SplitGroupService splitGroupService) {
        this.createSplitGroupUseCase = createSplitGroupUseCase;
        this.splitGroupService = splitGroupService;
    }

    @GetMapping
    public List<SplitGroupResponseDTO> groups(@AuthenticationPrincipal Jwt principal) {
        return splitGroupService.groupsVisibleTo(UUID.fromString(principal.getSubject()));
    }

    @PostMapping
    public ResponseEntity<SplitGroupResponseDTO> createGroup(
            @RequestBody CreateSplitGroupRequestDTO request,
            @AuthenticationPrincipal Jwt principal
    ) {
        SplitGroup createdGroup = createSplitGroupUseCase.execute(
                request,
                UUID.fromString(principal.getSubject()));
        return ResponseEntity.ok(SplitGroupResponseDTO.from(createdGroup, UUID.fromString(principal.getSubject())));
    }

    @PutMapping("/{id}")
    public SplitGroupResponseDTO updateGroup(@PathVariable UUID id, @RequestBody CreateSplitGroupRequestDTO request,
            @AuthenticationPrincipal Jwt principal) {
        return splitGroupService.update(UUID.fromString(principal.getSubject()), id, request);
    }

    @GetMapping("/{id}/history")
    public List<SplitGroupService.HistoryView> history(@PathVariable UUID id, @AuthenticationPrincipal Jwt principal) {
        return splitGroupService.history(UUID.fromString(principal.getSubject()), id);
    }
}
