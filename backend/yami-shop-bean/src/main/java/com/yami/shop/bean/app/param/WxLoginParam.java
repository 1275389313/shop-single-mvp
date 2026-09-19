package com.yami.shop.bean.app.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class WxLoginParam {

    @NotBlank(message = "code不能为空")
    @Schema(description = "wx.login code；mock 模式下可填任意非空字符串，将作为 openId")
    private String code;

    @Schema(description = "可选昵称")
    private String nickName;
}
