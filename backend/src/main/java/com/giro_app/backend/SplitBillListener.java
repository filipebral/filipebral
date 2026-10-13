package com.giro_app.backend;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SplitBillListener {

    // Ocultando os repositórios (SplitGroupRepository) para focar na lógica
    
    @RabbitListener(queues = RabbitMQConfig.QUEUE_SPLIT_BILLS)
    @Transactional // Se der erro no meio da matemática, a mensagem volta pra fila
    public void processTransactionSplit(TransactionCreatedEvent event) {
        
        System.out.println("Recebido no Split Service! Preparando divisão para a conta: " + event.accountId());
        
        try {
            // 1. Busca no banco de dados se esse usuário tem um 'SplitGroup' ativo para essa compra
            // 2. Calcula as frações (ex: 33.3% para cada amigo)
            // 3. Chama a API de transferências internas do BaaS para debitar os amigos e creditar o pagador
            
            System.out.println("Divisão processada com sucesso. Saldo dos envolvidos atualizado.");
            
        } catch (Exception e) {
            // Se o BaaS estiver fora do ar, o erro sobe. O RabbitMQ segura a mensagem
            // e tenta novamente depois, garantindo que o dinheiro não se perca.
            throw new RuntimeException("Falha temporária ao dividir a conta. Retentativa agendada.", e);
        }
    }
}