package com.yami.shop.bean.app.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class OrderRefundAuditParam {

    @NotNull(message = "退款单ID不能为空")
    @Schema(description = "退款记录ID")
    private Long refundId;

    @NotNull(message = "审核结果不能为空")
    @Schema(description = "2同意 3拒绝")
    private Integer refundSts;

    @Schema(description = "卖家备注 / 拒绝原因")
    private String sellerMsg;
}
