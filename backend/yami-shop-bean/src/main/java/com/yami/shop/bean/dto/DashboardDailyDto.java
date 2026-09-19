package com.yami.shop.bean.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 按自然日的已付 GMV / 成功退款。日期为 Asia/Shanghai 的 yyyy-MM-dd。
 */
@Data
@Schema(description = "数据看板按日明细")
public class DashboardDailyDto {

    @Schema(description = "自然日 yyyy-MM-dd")
    private String statDate;

    @Schema(description = "当日 pay_time 且已支付的订单数")
    private Long paidOrderCount;

    @Schema(description = "当日 GMV（已付 actual_total 之和）")
    private Double gmv;

    @Schema(description = "当日退款成功笔数")
    private Long refundCount;

    @Schema(description = "当日退款成功金额")
    private Double refundAmount;
}
