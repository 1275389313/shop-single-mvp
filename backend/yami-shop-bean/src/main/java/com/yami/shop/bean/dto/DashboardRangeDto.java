package com.yami.shop.bean.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 一个统计窗口（今日 / 近7日 / 近30日 / 累计）的汇总。
 */
@Data
@Schema(description = "数据看板区间汇总")
public class DashboardRangeDto {

    @Schema(description = "TODAY / LAST_7D / LAST_30D / ALL")
    private String code;

    @Schema(description = "展示名")
    private String label;

    @Schema(description = "窗口开始（含）")
    private Date startTime;

    @Schema(description = "窗口结束（不含）")
    private Date endTime;

    @Schema(description = "区间内下单数（按 create_time，含未付/关闭）")
    private Long orderCount;

    @Schema(description = "区间内下单且当前仍待付款（status=1）")
    private Long unpaidOrderCount;

    @Schema(description = "区间内支付成功单数（按 pay_time 且 is_payed=1）")
    private Long paidOrderCount;

    @Schema(description = "区间内下单且当前已关闭（status=6）")
    private Long closedOrderCount;

    @Schema(description = "待付款金额（status=1 的 actual_total）")
    private Double unpaidAmount;

    @Schema(description = "GMV：已支付订单 actual_total 之和（按 pay_time，含 mock 支付，不含退款冲减）")
    private Double gmv;

    @Schema(description = "退款成功金额（return_money_sts=1，按 refund_time）")
    private Double refundAmount;

    @Schema(description = "退款成功笔数")
    private Long refundCount;

    @Schema(description = "处理中退款金额（return_money_sts=0，按 apply_time）")
    private Double pendingRefundAmount;

    @Schema(description = "处理中退款笔数")
    private Long pendingRefundCount;

    @Schema(description = "净额 = GMV - 退款成功金额（处理中退款未扣）")
    private Double netGmv;
}
