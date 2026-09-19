package com.yami.shop.bean.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "库存预警配置与计数")
public class StockAlertConfigDto {

    @Schema(description = "全局阈值，SKU 可售库存 <= 该值预警")
    private Integer globalThreshold;

    @Schema(description = "当前店铺上架 SKU 低于阈值的数量")
    private Long count;
}
