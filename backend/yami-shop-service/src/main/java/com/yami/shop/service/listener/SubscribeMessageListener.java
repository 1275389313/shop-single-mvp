package com.yami.shop.service.listener;

import com.yami.shop.bean.event.DeliveryOrderEvent;
import com.yami.shop.bean.event.PaySuccessOrderEvent;
import com.yami.shop.bean.model.Order;
import com.yami.shop.service.SubscribeMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * After mock/real pay success and after admin ship. Runs after commit so WeChat errors cannot roll back the order.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscribeMessageListener {

    private final SubscribeMessageService subscribeMessageService;

    @Async("subscribeMessageExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onPaySuccess(PaySuccessOrderEvent event) {
        if (event == null || event.getOrders() == null) {
            return;
        }
        for (Order order : event.getOrders()) {
            try {
                subscribeMessageService.notifyPaySuccess(order);
            } catch (Exception ex) {
                log.warn("subscribe pay listener failed order={}",
                        order == null ? null : order.getOrderNumber());
            }
        }
    }

    @Async("subscribeMessageExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onDelivery(DeliveryOrderEvent event) {
        if (event == null || event.getOrder() == null) {
            return;
        }
        try {
            subscribeMessageService.notifyShipment(event.getOrder());
        } catch (Exception ex) {
            log.warn("subscribe ship listener failed order={}", event.getOrder().getOrderNumber());
        }
    }
}
