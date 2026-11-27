package com.giro_app.backend;

import java.util.Locale;
import java.util.regex.Pattern;

public final class CnpjValidator {
    private static final Pattern UNFORMATTED = Pattern.compile("[0-9A-Za-z]{12}[0-9]{2}");
    private static final Pattern FORMATTED = Pattern.compile("[0-9A-Za-z]{2}\\.[0-9A-Za-z]{3}\\.[0-9A-Za-z]{3}/[0-9A-Za-z]{4}-[0-9]{2}");
    private static final int[] FIRST_WEIGHTS = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] SECOND_WEIGHTS = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    private CnpjValidator() {}

    public static boolean isValid(String input) {
        if (input == null) return false;
        if (!UNFORMATTED.matcher(input).matches() && !FORMATTED.matcher(input).matches()) return false;
        String value = input.toUpperCase(Locale.ROOT);
        String cnpj = value.replace(".", "").replace("/", "").replace("-", "");
        if (cnpj.chars().distinct().count() == 1) return false;
        return cnpj.charAt(12) - '0' == digit(cnpj, FIRST_WEIGHTS)
                && cnpj.charAt(13) - '0' == digit(cnpj, SECOND_WEIGHTS);
    }

    private static int digit(String value, int[] weights) {
        int sum = 0;
        for (int i = 0; i < weights.length; i++) {
            sum += (value.charAt(i) - '0') * weights[i];
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}
