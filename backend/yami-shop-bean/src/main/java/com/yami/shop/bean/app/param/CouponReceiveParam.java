package com.yami.shop.bean.app.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "领取优惠券")
public class CouponReceiveParam {

    @NotNull(message = "优惠券不能为空")
    @Schema(description = "优惠券模板ID")
    private Long couponId;
}
