package com.giro_app.backend;

import org.springframework.stereotype.Component;

@Component
public class EventPublisherImpl implements EventPublisher {
    
    @Override
    public void publish(Object event) {
        System.out.println("Mock Event Publisher acionado: Evento disparado! Tipo: " + event.getClass().getSimpleName());
    }
}