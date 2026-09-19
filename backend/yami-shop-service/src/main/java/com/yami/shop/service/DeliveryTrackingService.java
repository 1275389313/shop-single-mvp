package com.yami.shop.service;

import com.yami.shop.bean.app.dto.DeliveryDto;

/**
 * Buyer/admin shipment and return-goods tracking. Real 快递100 when keys are set, otherwise mock.
 */
public interface DeliveryTrackingService {

    DeliveryDto queryShipmentForUser(String userId, String orderNumber);

    DeliveryDto queryReturnForUser(String userId, String refundSn);

    DeliveryDto queryShipmentForShop(Long shopId, String orderNumber);

    DeliveryDto queryReturnForShop(Long shopId, Long refundId);
}
