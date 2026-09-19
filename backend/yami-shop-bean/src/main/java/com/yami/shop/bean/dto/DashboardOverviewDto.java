package com.yami.shop.bean.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 管理端简易数据看板。开源 mall4j 没有统计报表接口，本 DTO 由订单/退款表聚合得到。
 */
@Data
@Schema(description = "数据看板总览")
public class DashboardOverviewDto {

    @Schema(description = "统计时区")
    private String timezone;

    @Schema(description = "生成时间")
    private Date generatedAt;

    @Schema(description = "当前是否 mock 支付；为 true 时 GMV 含当场已付的联调单")
    private Boolean mockPay;

    @Schema(description = "口径说明")
    private String note;

    @Schema(description = "今日 / 近7日 / 近30日 / 累计")
    private List<DashboardRangeDto> ranges;

    @Schema(description = "近30个自然日的已付 GMV 与退款（补零）")
    private List<DashboardDailyDto> daily;
}
