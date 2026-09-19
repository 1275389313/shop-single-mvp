package com.yami.shop.bean.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StockAlertRulesTest {

    @Test
    void parseGlobalFallsBackToDefault() {
        assertEquals(10, StockAlertRules.parseGlobal(null));
        assertEquals(10, StockAlertRules.parseGlobal(""));
        assertEquals(10, StockAlertRules.parseGlobal("abc"));
        assertEquals(10, StockAlertRules.parseGlobal("-1"));
        assertEquals(0, StockAlertRules.parseGlobal("0"));
        assertEquals(5, StockAlertRules.parseGlobal(" 5 "));
    }

    @Test
    void requireGlobalRejectsNegative() {
        assertThrows(IllegalArgumentException.class, () -> StockAlertRules.requireGlobal(-1));
        assertThrows(IllegalArgumentException.class, () -> StockAlertRules.requireGlobal(null));
        assertEquals(0, StockAlertRules.requireGlobal(0));
    }

    @Test
    void skuThresholdNullFollowsGlobal() {
        assertNull(StockAlertRules.normalizeSkuThreshold(null));
        assertEquals(Integer.valueOf(-1), StockAlertRules.normalizeSkuThreshold(-1));
        assertEquals(Integer.valueOf(3), StockAlertRules.normalizeSkuThreshold(3));
        assertThrows(IllegalArgumentException.class, () -> StockAlertRules.normalizeSkuThreshold(-2));
    }

    @Test
    void effectiveThreshold() {
        assertEquals(Integer.valueOf(10), StockAlertRules.effectiveThreshold(null, 10));
        assertEquals(Integer.valueOf(2), StockAlertRules.effectiveThreshold(2, 10));
        assertNull(StockAlertRules.effectiveThreshold(-1, 10));
    }

    @Test
    void lowStockUsesSaleableStocks() {
        assertTrue(StockAlertRules.isLowStock(0, null, 10));
        assertTrue(StockAlertRules.isLowStock(10, null, 10));
        assertFalse(StockAlertRules.isLowStock(11, null, 10));
        assertFalse(StockAlertRules.isLowStock(-1, null, 10));
        assertFalse(StockAlertRules.isLowStock(null, null, 10));
        assertTrue(StockAlertRules.isLowStock(1, 1, 10));
        assertFalse(StockAlertRules.isLowStock(2, 1, 10));
        assertFalse(StockAlertRules.isLowStock(0, -1, 10));
        assertTrue(StockAlertRules.isLowStock(0, 0, 10));
        assertFalse(StockAlertRules.isLowStock(1, 0, 10));
    }
}
