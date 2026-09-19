package com.yami.shop.api.listener;

import com.yami.shop.bean.app.dto.ShopCartOrderDto;
import com.yami.shop.bean.event.SubmitOrderEvent;
import com.yami.shop.bean.order.SubmitOrderOrder;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.service.CouponService;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 提交订单时核销已选优惠券。排在默认建单之后，此时已有 orderNumber。
 */
@Component
@AllArgsConstructor
public class SubmitCouponListener {

    private final CouponService couponService;

    @EventListener(SubmitOrderEvent.class)
    @Order(SubmitOrderOrder.COUPON)
    public void onSubmit(SubmitOrderEvent event) {
        String userId = SecurityUtils.getUser().getUserId();
        if (event.getMergerOrder() == null || event.getMergerOrder().getShopCartOrders() == null) {
            return;
        }
        for (ShopCartOrderDto shopCartOrderDto : event.getMergerOrder().getShopCartOrders()) {
            couponService.useOnSubmit(shopCartOrderDto, userId);
        }
    }
}
