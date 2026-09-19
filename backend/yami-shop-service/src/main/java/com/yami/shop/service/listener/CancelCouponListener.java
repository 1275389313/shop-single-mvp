package com.yami.shop.service.listener;

import com.yami.shop.bean.event.CancelOrderEvent;
import com.yami.shop.service.CouponService;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 未支付取消 / 超时关单：退回已核销优惠券。放在 service 以便 api 与 admin 定时关单都会触发。
 */
@Component
@AllArgsConstructor
public class CancelCouponListener {

    private final CouponService couponService;

    @EventListener(CancelOrderEvent.class)
    public void onCancel(CancelOrderEvent event) {
        if (event.getOrder() == null) {
            return;
        }
        couponService.restoreByOrderNumber(event.getOrder().getOrderNumber());
    }
}
