package com.stockpilot.backend.forecast;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ForecastMathTest {
    @Test
    void computesDaysOfCoverFromStockAndVelocity() {
        assertEquals(new BigDecimal("4.00"), ForecastMath.daysOfCover(
                new BigDecimal("12"), new BigDecimal("3")));
    }

    @Test
    void recommendsEnoughForLeadTimeAndTargetCoverRoundedToPackSize() {
        BigDecimal recommended = ForecastMath.recommendedQuantity(
                new BigDecimal("6"), 4, 4, new BigDecimal("9"), new BigDecimal("24"));

        assertEquals(new BigDecimal("48"), recommended);
    }

    @Test
    void neverRecommendsNegativeQuantity() {
        assertEquals(BigDecimal.ZERO, ForecastMath.recommendedQuantity(
                new BigDecimal("1"), 2, 7, new BigDecimal("20"), new BigDecimal("6")));
    }
}
