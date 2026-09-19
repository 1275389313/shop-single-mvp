package com.yami.shop.service.task;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.yami.shop.bean.enums.OrderStatus;
import com.yami.shop.bean.model.Order;
import com.yami.shop.bean.model.OrderItem;
import com.yami.shop.common.config.ShopMvpProperties;
import com.yami.shop.service.OrderService;
import com.yami.shop.service.ProductService;
import com.yami.shop.service.SkuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Local stand-in for upstream xxl-job handlers (those remain commented in admin XxlJobConfig).
 * Uses a Redis lock so api + admin can both boot without double-closing orders.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderAutoCloseScheduler {

    private static final String LOCK_CANCEL = "shop-mvp:lock:order-cancel";
    private static final String LOCK_CONFIRM = "shop-mvp:lock:order-confirm";

    private final ShopMvpProperties shopMvpProperties;
    private final OrderService orderService;
    private final ProductService productService;
    private final SkuService skuService;
    private final StringRedisTemplate stringRedisTemplate;

    @Scheduled(cron = "0 */1 * * * ?")
    public void cancelUnpaidOrders() {
        if (!shopMvpProperties.getOrder().isAutoCloseEnabled()) {
            return;
        }
        if (!tryLock(LOCK_CANCEL, 50)) {
            return;
        }
        Date now = new Date();
        int minutes = shopMvpProperties.getOrder().getAutoCloseMinutes();
        List<Order> orders = orderService.listOrderAndOrderItems(
                OrderStatus.UNPAY.value(), DateUtil.offsetMinute(now, -minutes));
        if (CollectionUtil.isEmpty(orders)) {
            return;
        }
        log.info("auto-close unpaid orders count={} minutes={}", orders.size(), minutes);
        orderService.cancelOrders(orders);
        evictCaches(orders);
    }

    @Scheduled(cron = "0 0 */1 * * ?")
    public void autoConfirmOrders() {
        if (!shopMvpProperties.getOrder().isAutoCloseEnabled()) {
            return;
        }
        if (!tryLock(LOCK_CONFIRM, 3500)) {
            return;
        }
        Date now = new Date();
        int days = shopMvpProperties.getOrder().getAutoConfirmDays();
        List<Order> orders = orderService.listOrderAndOrderItems(
                OrderStatus.CONSIGNMENT.value(), DateUtil.offsetDay(now, -days));
        if (CollectionUtil.isEmpty(orders)) {
            return;
        }
        log.info("auto-confirm receive orders count={} days={}", orders.size(), days);
        orderService.confirmOrder(orders);
        evictCaches(orders);
    }

    private void evictCaches(List<Order> orders) {
        for (Order order : orders) {
            List<OrderItem> orderItems = order.getOrderItems();
            if (orderItems == null) {
                continue;
            }
            for (OrderItem orderItem : orderItems) {
                productService.removeProductCacheByProdId(orderItem.getProdId());
                skuService.removeSkuCacheBySkuId(orderItem.getSkuId(), orderItem.getProdId());
            }
        }
    }

    private boolean tryLock(String key, long seconds) {
        Boolean ok = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", seconds, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(ok);
    }
}
