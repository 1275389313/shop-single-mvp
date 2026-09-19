package com.yami.shop.bean.param;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class StockAlertSkuParam {

    @NotNull(message = "skuId 不能为空")
    private Long skuId;

    /**
     * null 跟随全局；-1 该 SKU 不预警；&gt;=0 自定义阈值。
     */
    private Integer stocksArm;
}
