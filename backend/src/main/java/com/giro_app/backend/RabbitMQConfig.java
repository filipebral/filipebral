package com.giro_app.backend;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "giro.financial.exchange";
    public static final String QUEUE_SPLIT_BILLS = "giro.split-bills.queue";
    public static final String ROUTING_KEY_TRANSACTION = "transaction.created";

    // 1. A Caixa Postal onde o serviço de Divisão de Contas vai escutar
    @Bean
    public Queue splitBillsQueue() {
        return new Queue(QUEUE_SPLIT_BILLS, true); // true = Duração persistente (salva no disco)
    }

    // 2. A Agência de distribuição de eventos
    @Bean
    public TopicExchange financialExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    // 3. A Regra: "Tudo que tiver a chave 'transaction.created' vai para a fila de split"
    @Bean
    public Binding bindingSplitBills(Queue splitBillsQueue, TopicExchange financialExchange) {
        return BindingBuilder.bind(splitBillsQueue).to(financialExchange).with(ROUTING_KEY_TRANSACTION);
    }

    // 4. Converte os nossos objetos Java (Eventos) automaticamente para JSON
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}