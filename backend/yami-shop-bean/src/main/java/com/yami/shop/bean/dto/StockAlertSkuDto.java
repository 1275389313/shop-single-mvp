package com.yami.shop.bean.dto;

import tools.jackson.databind.annotation.JsonSerialize;
import com.yami.shop.common.serializer.json.ImgJsonSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理端库存预警列表行（SKU 粒度）。
 */
@Data
@Schema(description = "低库存 SKU")
public class StockAlertSkuDto {

    private Long skuId;
    private Long prodId;
    private String prodName;
    @JsonSerialize(using = ImgJsonSerializer.class)
    private String pic;
    private Integer prodStatus;
    private String skuName;
    private String properties;
    @Schema(description = "可售库存 tz_sku.stocks，-1 无限")
    private Integer stocks;
    private Integer actualStocks;
    @Schema(description = "SKU 阈值，null 跟随全局，-1 关闭")
    private Integer stocksArm;
    @Schema(description = "本次比较使用的阈值")
    private Integer effectiveThreshold;
}
