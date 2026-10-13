package com.giro_app.backend;

import java.util.UUID;

public record AuthResponseDTO(String token, long expiresIn, UserDTO user) {
    public record UserDTO(UUID id, String name, String email) {
    }
}
