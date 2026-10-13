package com.giro_app.backend;
import org.springframework.stereotype.Component;

@Component
public class BaaSGatewayImpl implements BaaSGateway {

    private final BaaSFeignClient feignClient;

    public BaaSGatewayImpl(BaaSFeignClient feignClient) {
        this.feignClient = feignClient;
    }

    @Override
    public BaaSAccountResponse createAccountForCustomer(String baasCustomerId) {
        // Monta o payload esperado pelo parceiro
        var requestPayload = new BaaSAccountRequestDTO(baasCustomerId, "CHECKING");
        
        // Dispara a requisição HTTP real
        return feignClient.createAccount(requestPayload);
    }
}