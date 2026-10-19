package com.liverpool.qa.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PriceUtils {

    private PriceUtils() {
    }

    /**
     * Convierte un texto de precio como "$10,535.15" o "$8,499.15" a BigDecimal exacto.
     */
    public static BigDecimal parsePrice(String rawPrice) {
        if (rawPrice == null || rawPrice.isBlank()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        String clean = rawPrice.replaceAll("[^0-9.]", "");
        if (clean.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return new BigDecimal(clean).setScale(2, RoundingMode.HALF_UP);
    }
}
