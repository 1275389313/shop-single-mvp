package com.yami.shop.bean.app.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class MockPayNoticeParam {

    @NotBlank(message = "payNo不能为空")
    @Schema(description = "内部支付流水号")
    private String payNo;

    @Schema(description = "模拟的第三方交易号")
    private String bizPayNo;
}
