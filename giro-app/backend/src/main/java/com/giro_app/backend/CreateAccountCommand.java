package com.giro_app.backend;
import java.util.UUID;

// Records do Java 14+ são perfeitos para comandos imutáveis
public record CreateAccountCommand(
    UUID userId, 
    String baasCustomerId // O ID do cliente já validado no KYC anteriormente
) {}