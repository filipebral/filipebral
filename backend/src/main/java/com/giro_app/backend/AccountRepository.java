package com.giro_app.backend;
import java.util.UUID;
import java.util.Optional;

// Porta de saída para o Banco de Dados
public interface AccountRepository {
    Account save(Account account);
    Optional findById(UUID id);
}

// Porta de saída para a API do provedor BaaS
interface BaaSGateway {
    BaaSAccountResponse createAccountForCustomer(String baasCustomerId);
}

// Porta de saída para a fila de mensageria (Kafka/RabbitMQ)
interface EventPublisher {
    void publish(Object event);
}