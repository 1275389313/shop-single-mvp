package com.yami.shop.api.listener;

import com.yami.shop.bean.app.dto.ShopCartOrderDto;
import com.yami.shop.bean.event.ConfirmOrderEvent;
import com.yami.shop.bean.order.ConfirmOrderOrder;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.service.CouponService;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 确认订单时套用优惠券。排在默认金额计算之后。
 */
@Component
@AllArgsConstructor
public class ConfirmCouponListener {

    private final CouponService couponService;

    @EventListener(ConfirmOrderEvent.class)
    @Order(ConfirmOrderOrder.COUPON)
    public void onConfirm(ConfirmOrderEvent event) {
        ShopCartOrderDto shopCartOrderDto = event.getShopCartOrderDto();
        String userId = SecurityUtils.getUser().getUserId();
        couponService.applyToConfirm(shopCartOrderDto, event.getOrderParam(), userId);
    }
}
