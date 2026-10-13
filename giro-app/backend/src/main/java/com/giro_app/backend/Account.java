package com.giro_app.backend;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter // Usamos Lombok para os Getters, mas NUNCA @Setter na classe toda em DDD
@NoArgsConstructor // Exigência do Hibernate
public class Account {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Convert(converter = SensitiveDataConverter.class)
    @Column(name = "baas_account_id", unique = true, length = 1024)
    private String baasAccountId;

    @Convert(converter = SensitiveDataConverter.class)
    @Column(length = 1024)
    private String routingNumber;

    @Convert(converter = SensitiveDataConverter.class)
    @Column(length = 1024)
    private String branch;

    @Convert(converter = SensitiveDataConverter.class)
    @Column(length = 1024)
    private String accountNumber;

    @Column(name = "available_balance", precision = 19, scale = 4)
    private BigDecimal availableBalance;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    public UUID getId() {
        return id;
    }
    
    // Construtor principal para abertura de conta
    public Account(UUID userId) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.availableBalance = BigDecimal.ZERO;
        this.status = AccountStatus.PENDING; // Nasce pendente até o BaaS confirmar
    }

    // Comportamento (Regra de Negócio): Só o domínio sabe como ativar a si mesmo
    public void activate(String baasAccountId, String routing, String branch, String number) {
        if (this.status != AccountStatus.PENDING) {
            throw new IllegalStateException("Apenas contas pendentes podem ser ativadas.");
        }
        this.baasAccountId = baasAccountId;
        this.routingNumber = routing;
        this.branch = branch;
        this.accountNumber = number;
        this.status = AccountStatus.ACTIVE;
    }
    
    // Outros comportamentos futuros: credit(BigDecimal amount), debit(BigDecimal amount)
}