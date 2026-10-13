package com.giro_app.backend;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// A URL aqui virá do application.properties (ex: baas.api.url=https://sandbox.api.starkbank.com/v2)
@FeignClient(name = "baasClient", url = "${baas.api.url}")
public interface BaaSFeignClient {

    @PostMapping("/accounts")
    BaaSAccountResponse createAccount(@RequestBody BaaSAccountRequestDTO request);
}

// DTO interno para enviar ao BaaS
record BaaSAccountRequestDTO(String customerId, String accountType) {}