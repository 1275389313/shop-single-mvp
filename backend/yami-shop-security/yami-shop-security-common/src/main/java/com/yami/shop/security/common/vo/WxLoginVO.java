package com.yami.shop.security.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class WxLoginVO extends TokenInfoVO {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "昵称")
    private String nickName;

    @Schema(description = "头像")
    private String pic;

    @Schema(description = "是否走了 mock 登录")
    private boolean mock;
}
