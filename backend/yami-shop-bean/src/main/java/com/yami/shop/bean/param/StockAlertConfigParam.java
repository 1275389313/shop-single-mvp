package com.yami.shop.bean.param;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class StockAlertConfigParam {

    @NotNull(message = "请填写全局库存预警阈值")
    private Integer globalThreshold;
}
