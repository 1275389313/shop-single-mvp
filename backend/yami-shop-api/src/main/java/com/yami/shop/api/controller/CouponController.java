package com.yami.shop.api.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.bean.app.dto.CouponDto;
import com.yami.shop.bean.app.dto.ProductDto;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.service.CouponService;
import com.yami.shop.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公开领券中心（无需登录）。领取走 /p/coupon。
 */
@RestController
@RequestMapping("/coupon")
@Tag(name = "优惠券")
@AllArgsConstructor
public class CouponController {

    private final CouponService couponService;
    private final ProductService productService;

    @GetMapping("/list")
    @Operation(summary = "领券中心")
    public ServerResponseEntity<IPage<CouponDto>> list(PageParam<CouponDto> page) {
        return ServerResponseEntity.success(couponService.pageCenter(page));
    }

    @GetMapping("/prodListByCouponId")
    @Operation(summary = "优惠券适用商品，P1 为全店在售商品")
    @Parameter(name = "couponId", description = "优惠券模板ID")
    public ServerResponseEntity<IPage<ProductDto>> prodListByCouponId(
            @RequestParam("couponId") Long couponId,
            PageParam<ProductDto> page) {
        if (couponService.getById(couponId) == null) {
            return ServerResponseEntity.success(page);
        }
        return ServerResponseEntity.success(productService.pageByPutAwayTime(page));
    }
}
