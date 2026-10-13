package com.giro_app.backend;
import jakarta.validation.constraints.NotBlank;

public record AccountRequestDTO(
    @NotBlank(message = "O ID do cliente no BaaS é obrigatório")
    String baasCustomerId
) {}