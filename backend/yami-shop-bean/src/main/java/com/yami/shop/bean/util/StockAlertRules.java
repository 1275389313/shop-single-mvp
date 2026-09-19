package com.yami.shop.bean.util;

/**
 * 库存预警规则。比较的是 mall4j SKU 可售库存 {@code tz_sku.stocks}（下单扣减的字段，-1 表示无限）。
 * <p>
 * 阈值：全局配置 {@code STOCK_ALERT_THRESHOLD}；SKU {@code stocks_arm} 非空则覆盖，-1 表示该 SKU 不预警。
 */
public final class StockAlertRules {

    public static final String CONFIG_KEY = "STOCK_ALERT_THRESHOLD";
    public static final int DEFAULT_GLOBAL_THRESHOLD = 10;
    public static final int DISABLE_SKU_THRESHOLD = -1;
    public static final int MAX_THRESHOLD = 999_999;

    private StockAlertRules() {
    }

    /**
     * 读取配置：空或非法时回落到默认 10，不抛错。
     */
    public static int parseGlobal(String raw) {
        if (raw == null || raw.isBlank()) {
            return DEFAULT_GLOBAL_THRESHOLD;
        }
        try {
            return requireGlobal(Integer.parseInt(raw.trim()));
        } catch (IllegalArgumentException ex) {
            return DEFAULT_GLOBAL_THRESHOLD;
        }
    }

    public static int requireGlobal(Integer value) {
        if (value == null || value < 0 || value > MAX_THRESHOLD) {
            throw new IllegalArgumentException("全局库存预警阈值须为 0～" + MAX_THRESHOLD + " 的整数");
        }
        return value;
    }

    /**
     * @return 规范化后的 SKU 阈值；null 表示跟随全局
     */
    public static Integer normalizeSkuThreshold(Integer stocksArm) {
        if (stocksArm == null) {
            return null;
        }
        if (stocksArm == DISABLE_SKU_THRESHOLD) {
            return DISABLE_SKU_THRESHOLD;
        }
        if (stocksArm < 0 || stocksArm > MAX_THRESHOLD) {
            throw new IllegalArgumentException("SKU 预警阈值须为空（跟随全局）、-1（不预警）或 0～" + MAX_THRESHOLD);
        }
        return stocksArm;
    }

    /**
     * @return 生效阈值；null 表示该 SKU 关闭预警
     */
    public static Integer effectiveThreshold(Integer skuStocksArm, int globalThreshold) {
        if (skuStocksArm == null) {
            return globalThreshold;
        }
        if (skuStocksArm < 0) {
            return null;
        }
        return skuStocksArm;
    }

    /**
     * 无限库存（stocks &lt; 0）不算低库存。
     */
    public static boolean isLowStock(Integer stocks, Integer skuStocksArm, int globalThreshold) {
        if (stocks == null || stocks < 0) {
            return false;
        }
        Integer threshold = effectiveThreshold(skuStocksArm, globalThreshold);
        if (threshold == null) {
            return false;
        }
        return stocks <= threshold;
    }
}
