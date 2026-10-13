package com.giro_app.backend;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateAccountUseCase {

    private final AccountRepository accountRepository;
    private final BaaSGateway baasGateway;
    private final EventPublisher eventPublisher;

    // Injeção de dependência via construtor (obrigatório em Clean Architecture)
    public CreateAccountUseCase(
            AccountRepository accountRepository, 
            BaaSGateway baasGateway, 
            EventPublisher eventPublisher) {
        this.accountRepository = accountRepository;
        this.baasGateway = baasGateway;
        this.eventPublisher = eventPublisher;
    }

    @Transactional // Garante que se o RabbitMQ ou o Banco falharem, tudo sofre rollback
    public Account execute(CreateAccountCommand command) {
        
        // 1. Cria a entidade de domínio local (Estado: PENDING)
        Account newAccount = new Account(command.userId());
        
        // Salvamos antes para garantir a rastreabilidade caso a chamada do BaaS sofra timeout
        accountRepository.save(newAccount);

        try {
            // 2. Chama a API do parceiro BaaS
            var baasResponse = baasGateway.createAccountForCustomer(command.baasCustomerId());

            // 3. Aplica a regra de negócio de ativação
            newAccount.activate(
                baasResponse.baasAccountId(),
                baasResponse.routingNumber(),
                baasResponse.branch(),
                baasResponse.accountNumber()
            );

            // 4. Salva o estado atualizado (ACTIVE)
            Account savedAccount = accountRepository.save(newAccount);

            // 5. Dispara evento para outros microsserviços (ex: Mandar Push de boas vindas)
            eventPublisher.publish(new AccountCreatedEvent(savedAccount.getId(), command.userId()));

            return savedAccount;

        } catch (Exception e) {
            // Se o BaaS falhar, a conta fica PENDING e uma rotina (Job) pode tentar de novo depois
            // Aqui poderíamos disparar um AccountCreationFailedEvent
            throw new RuntimeException("Falha ao integrar com o BaaS. Detalhes: " + e.getMessage());
        }
    }
}

// O Evento que trafegará no Kafka
record AccountCreatedEvent(UUID accountId, UUID userId) {}