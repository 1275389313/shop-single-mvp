package com.yami.shop.bean.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Public template IDs for uni-app {@code wx.requestSubscribeMessage}. Never includes AppSecret.
 */
@Data
@Schema(description = "小程序订阅消息模板 ID（非密钥）")
public class SubscribeMessageConfigDto {

    @Schema(description = "支付成功模板 ID，空表示未配置")
    private String paySuccessTemplateId;

    @Schema(description = "发货通知模板 ID，空表示未配置")
    private String shipTemplateId;
}
