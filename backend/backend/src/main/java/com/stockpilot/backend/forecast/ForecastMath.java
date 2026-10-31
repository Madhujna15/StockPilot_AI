package com.stockpilot.backend.forecast;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class ForecastMath {
    private ForecastMath() { }

    public static BigDecimal daysOfCover(BigDecimal stock, BigDecimal dailyVelocity) {
        if (dailyVelocity.signum() <= 0) return null;
        return stock.divide(dailyVelocity, 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal recommendedQuantity(BigDecimal dailyVelocity, int leadTimeDays,
                                                  int targetCoverDays, BigDecimal currentStock,
                                                  BigDecimal packSize) {
        BigDecimal needed = dailyVelocity.multiply(BigDecimal.valueOf((long) leadTimeDays + targetCoverDays))
                .subtract(currentStock).max(BigDecimal.ZERO);
        if (needed.signum() == 0) return BigDecimal.ZERO;
        BigDecimal packs = needed.divide(packSize, 0, RoundingMode.CEILING);
        return packs.multiply(packSize).stripTrailingZeros();
    }
}
