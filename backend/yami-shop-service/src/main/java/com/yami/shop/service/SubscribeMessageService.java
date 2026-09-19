package com.yami.shop.service;

import com.yami.shop.bean.dto.SubscribeMessageConfigDto;
import com.yami.shop.bean.model.Order;

/**
 * WeChat mini-program subscribe messages. Missing template IDs or AppSecret → log skip, never fail pay/ship.
 */
public interface SubscribeMessageService {

    void notifyPaySuccess(Order order);

    void notifyShipment(Order order);

    /**
     * Template IDs only. Safe to expose to the mini-program.
     */
    SubscribeMessageConfigDto publicConfig();
}
