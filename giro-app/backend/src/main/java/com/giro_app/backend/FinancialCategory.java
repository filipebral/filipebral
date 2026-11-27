package com.giro_app.backend;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public enum FinancialCategory {
    FOOD("Alimentação"), TRANSPORT("Transporte"), HOUSING("Moradia"), HEALTH("Saúde"),
    EDUCATION("Educação"), LEISURE("Lazer"), SERVICES("Serviços"), SHOPPING("Compras"), OTHER("Outros");

    private final String label;
    FinancialCategory(String label) { this.label = label; }
    public String label() { return label; }

    public static FinancialCategory parse(String value) {
        try {
            return FinancialCategory.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoria financeira inválida.");
        }
    }

    public static List<CategoryView> views() {
        return Arrays.stream(values()).map(category -> new CategoryView(category.name(), category.label)).toList();
    }

    public record CategoryView(String id, String label) {}
}
