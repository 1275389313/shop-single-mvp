package com.yami.shop.bean.app.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderRefundReceiveParam {

    @NotNull(message = "退款单ID不能为空")
    @Schema(description = "退款记录ID")
    private Long refundId;

    @Schema(description = "收货备注（选填）")
    private String receiveMessage;
}
