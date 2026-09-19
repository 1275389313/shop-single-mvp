package com.yami.shop.bean.event;

import com.yami.shop.bean.model.Order;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Admin shipped an order (status → 待收货). Used by subscribe-message hooks.
 */
@Data
@AllArgsConstructor
public class DeliveryOrderEvent {

    private Order order;
}
