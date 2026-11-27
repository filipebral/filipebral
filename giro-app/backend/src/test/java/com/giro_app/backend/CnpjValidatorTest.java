package com.giro_app.backend;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CnpjValidatorTest {
    @Test
    void acceptsNumericAndAlphanumericCnpjsWithValidCheckDigits() {
        assertTrue(CnpjValidator.isValid("11.222.333/0001-81"));
        assertTrue(CnpjValidator.isValid("11222333000181"));
        assertTrue(CnpjValidator.isValid("12.ABC.345/01DE-35"));
        assertTrue(CnpjValidator.isValid("12abc34501de35"));
    }

    @Test
    void rejectsInvalidDigitsAndMalformedOrRepeatedValues() {
        assertFalse(CnpjValidator.isValid("11.222.333/0001-80"));
        assertFalse(CnpjValidator.isValid("12.ABC.345/01DE-36"));
        assertFalse(CnpjValidator.isValid("00.000.000/0000-00"));
        assertFalse(CnpjValidator.isValid("12ABC34501DE3A"));
        assertFalse(CnpjValidator.isValid("11-222333000181"));
        assertFalse(CnpjValidator.isValid("12.ABC.345/01DÉ-35"));
        assertFalse(CnpjValidator.isValid(null));
    }
}
