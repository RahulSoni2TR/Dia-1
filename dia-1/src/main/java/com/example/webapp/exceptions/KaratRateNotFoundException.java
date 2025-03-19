package com.example.webapp.exceptions;

import java.math.BigDecimal;

public class KaratRateNotFoundException extends RuntimeException {
    public KaratRateNotFoundException(BigDecimal karat) {
        super("[getKaratRate] Rate not found for karat value: " + karat);
    }
}