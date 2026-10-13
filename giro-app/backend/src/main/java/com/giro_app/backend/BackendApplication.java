package com.giro_app.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients; // <-- Importação necessária

@SpringBootApplication
@EnableFeignClients // <-- Ativa o motor do Feign Client para APIs externas
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}