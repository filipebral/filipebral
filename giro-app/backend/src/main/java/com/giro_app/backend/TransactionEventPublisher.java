package com.giro_app.backend;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransactionEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public TransactionEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishTransactionCreated(TransactionCreatedEvent event) {
        // Envia o JSON do evento para a Exchange, com a rota específica
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_NAME, 
            RabbitMQConfig.ROUTING_KEY_TRANSACTION, 
            event
        );
        System.out.println("Evento disparado para o RabbitMQ: Compra de " + event.amount());
    }
}

// O Objeto/Evento (implicando as bibliotecas Java 14+ Records)
record TransactionCreatedEvent(String accountId, String amount, String category) {}