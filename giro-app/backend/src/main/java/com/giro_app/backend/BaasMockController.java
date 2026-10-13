package com.giro_app.backend;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.annotation.Profile;
import java.math.BigDecimal;

@RestController
@RequestMapping("/mock-baas")
@Profile("!prod")
public class BaasMockController {

    @PostMapping("/accounts")
    public ResponseEntity<BaaSAccountResponse> mockCreateAccount(@RequestBody BaaSAccountRequestDTO request) {
        System.out.println(">> [MOCK BAAS] Recebeu requisição para o customer: " + request.customerId());
        
        // Retorna uma resposta fake de sucesso simulando o BaaS real
        BaaSAccountResponse fakeResponse = new BaaSAccountResponse(
            "baas_cust_" + System.currentTimeMillis(),
            "12345-6",
            "0001",
            "341",
            new BigDecimal("0.00"),
            "ACTIVE"
        );
        
        return ResponseEntity.ok(fakeResponse);
    }
}