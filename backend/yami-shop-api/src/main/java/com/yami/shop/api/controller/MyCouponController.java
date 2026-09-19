package com.yami.shop.api.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.bean.app.dto.CouponDto;
import com.yami.shop.bean.app.param.CouponReceiveParam;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户优惠券：领取、我的券。路径在 /p 下，需登录。
 */
@RestController
@RequestMapping("/p/coupon")
@Tag(name = "我的优惠券")
@AllArgsConstructor
public class MyCouponController {

    private final CouponService couponService;

    @PostMapping("/receive")
    @Operation(summary = "领取优惠券")
    public ServerResponseEntity<Void> receive(@Valid @RequestBody CouponReceiveParam param) {
        couponService.receive(SecurityUtils.getUser().getUserId(), param.getCouponId());
        return ServerResponseEntity.success();
    }

    @GetMapping("/myList")
    @Operation(summary = "我的优惠券，status：0未使用 1已使用 2已过期")
    public ServerResponseEntity<IPage<CouponDto>> myList(
            @RequestParam(value = "status", required = false) Integer status,
            PageParam<CouponDto> page) {
        return ServerResponseEntity.success(
                couponService.pageMine(SecurityUtils.getUser().getUserId(), status, page));
    }
}
