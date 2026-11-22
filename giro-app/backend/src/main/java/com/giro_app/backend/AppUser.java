package com.giro_app.backend;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    private UUID id;

    @Convert(converter = SensitiveDataConverter.class)
    @Column(length = 255)
    private String cpf;

    @Column(name = "cpf_lookup_hash", unique = true, length = 64)
    private String cpfLookupHash;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    protected AppUser() {
    }

    public AppUser(String cpf, String cpfLookupHash, String name, String passwordHash) {
        this.id = UUID.randomUUID();
        this.cpf = cpf;
        this.cpfLookupHash = cpfLookupHash;
        this.name = name;
        this.passwordHash = passwordHash;
    }

    public UUID getId() {
        return id;
    }

    public String getCpf() {
        return cpf;
    }

    public String getName() {
        return name;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
