package com.yami.shop.service.impl;

import cn.hutool.core.util.StrUtil;
import com.yami.shop.bean.app.dto.DeliveryDto;
import com.yami.shop.bean.enums.OrderStatus;
import com.yami.shop.bean.enums.RefundFlow;
import com.yami.shop.bean.model.Delivery;
import com.yami.shop.bean.model.Order;
import com.yami.shop.bean.model.OrderRefund;
import com.yami.shop.bean.model.UserAddrOrder;
import com.yami.shop.bean.util.ExpressCompanyCode;
import com.yami.shop.bean.util.MockExpressTrack;
import com.yami.shop.common.config.ShopMvpProperties;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.service.DeliveryService;
import com.yami.shop.service.DeliveryTrackingService;
import com.yami.shop.service.OrderRefundService;
import com.yami.shop.service.OrderService;
import com.yami.shop.service.UserAddrOrderService;
import com.yami.shop.service.express.ExpressQueryClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryTrackingServiceImpl implements DeliveryTrackingService {

    private static final String SHOP_ORIGIN = "广州市";

    private final OrderService orderService;
    private final OrderRefundService orderRefundService;
    private final DeliveryService deliveryService;
    private final UserAddrOrderService userAddrOrderService;
    private final ShopMvpProperties shopMvpProperties;
    private final ExpressQueryClient expressQueryClient;

    @Override
    public DeliveryDto queryShipmentForUser(String userId, String orderNumber) {
        return toShipment(ownedOrder(userId, orderNumber));
    }

    @Override
    public DeliveryDto queryReturnForUser(String userId, String refundSn) {
        return toReturn(orderRefundService.getByRefundSn(userId, refundSn));
    }

    @Override
    public DeliveryDto queryShipmentForShop(Long shopId, String orderNumber) {
        Order order = orderService.getOrderByOrderNumber(orderNumber);
        if (order == null || !java.util.Objects.equals(shopId, order.getShopId())) {
            throw new YamiShopBindException("订单不存在");
        }
        return toShipment(order);
    }

    @Override
    public DeliveryDto queryReturnForShop(Long shopId, Long refundId) {
        return toReturn(orderRefundService.getShopRefund(shopId, refundId));
    }

    private Order ownedOrder(String userId, String orderNumber) {
        Order order = orderService.getOrderByOrderNumber(orderNumber);
        if (order == null || !java.util.Objects.equals(order.getUserId(), userId)) {
            throw new YamiShopBindException("订单不存在");
        }
        return order;
    }

    private DeliveryDto toShipment(Order order) {
        Delivery company = order.getDvyId() == null ? null : deliveryService.getById(order.getDvyId());
        String companyName = company != null ? company.getDvyName() : "快递";
        String trackingNo = StrUtil.blankToDefault(order.getDvyFlowId(), "");
        if (StrUtil.isBlank(trackingNo)) {
            return decorate(MockExpressTrack.empty(companyName, "", MockExpressTrack.DIRECTION_SHIPMENT, "订单尚未发货或未填写运单号"),
                    company);
        }
        UserAddrOrder addr = order.getAddrOrderId() == null ? null : userAddrOrderService.getById(order.getAddrOrderId());
        String dest = place(addr);
        String phone = addr == null ? null : addr.getMobile();
        String companyCode = ExpressCompanyCode.resolve(
                company == null ? null : company.getQueryUrl(), companyName);
        boolean signed = java.util.Objects.equals(order.getStatus(), OrderStatus.CONFIRM.value())
                || java.util.Objects.equals(order.getStatus(), OrderStatus.SUCCESS.value());
        return queryOrMock(MockExpressTrack.DIRECTION_SHIPMENT, company, companyName, companyCode, trackingNo,
                phone, order.getDvyTime(), signed, SHOP_ORIGIN, dest);
    }

    private DeliveryDto toReturn(OrderRefund refund) {
        if (refund == null) {
            throw new YamiShopBindException("退款单不存在");
        }
        RefundFlow.fill(refund);
        String companyName = StrUtil.blankToDefault(refund.getExpressName(), "快递");
        String trackingNo = StrUtil.blankToDefault(refund.getExpressNo(), "");
        Delivery company = matchCompany(companyName);
        if (StrUtil.isBlank(trackingNo)) {
            return decorate(MockExpressTrack.empty(companyName, "", MockExpressTrack.DIRECTION_RETURN, "尚未填写退货单号"),
                    company);
        }
        Order order = orderService.getOrderByOrderNumber(refund.getOrderNumber());
        UserAddrOrder addr = order == null || order.getAddrOrderId() == null
                ? null : userAddrOrderService.getById(order.getAddrOrderId());
        String from = place(addr);
        String phone = addr == null ? null : addr.getMobile();
        String companyCode = ExpressCompanyCode.resolve(
                company == null ? null : company.getQueryUrl(), companyName);
        boolean signed = refund.getReceiveTime() != null
                || java.util.Objects.equals(refund.getReturnMoneySts(), RefundFlow.MONEY_SUCCESS);
        return queryOrMock(MockExpressTrack.DIRECTION_RETURN, company, companyName, companyCode, trackingNo,
                phone, refund.getShipTime(), signed, from, SHOP_ORIGIN);
    }

    private DeliveryDto queryOrMock(String direction, Delivery company, String companyName, String companyCode,
                                    String trackingNo, String phone, Date shipTime, boolean signed,
                                    String fromPlace, String toPlace) {
        ShopMvpProperties.Kuaidi100 conf = shopMvpProperties.getKuaidi100();
        if (conf != null && conf.isConfigured()) {
            DeliveryDto dto = expressQueryClient.query(companyCode, trackingNo, phone);
            dto.setDirection(direction);
            dto.setCompanyName(companyName);
            if (StrUtil.isBlank(dto.getCompanyHomeUrl()) && company != null) {
                dto.setCompanyHomeUrl(company.getCompanyHomeUrl());
            }
            if (StrUtil.isBlank(dto.getDvyFlowId())) {
                dto.setDvyFlowId(trackingNo);
            }
            return dto;
        }
        DeliveryDto dto = MockExpressTrack.DIRECTION_RETURN.equals(direction)
                ? MockExpressTrack.returned(companyName, trackingNo, shipTime, new Date(), signed, fromPlace, toPlace)
                : MockExpressTrack.shipment(companyName, trackingNo, shipTime, new Date(), signed, fromPlace, toPlace);
        dto.setCompanyCode(companyCode);
        return decorate(dto, company);
    }

    private DeliveryDto decorate(DeliveryDto dto, Delivery company) {
        if (company != null) {
            dto.setCompanyHomeUrl(company.getCompanyHomeUrl());
            if (StrUtil.isBlank(dto.getCompanyName())) {
                dto.setCompanyName(company.getDvyName());
            }
            if (StrUtil.isBlank(dto.getCompanyCode())) {
                dto.setCompanyCode(ExpressCompanyCode.fromQueryUrl(company.getQueryUrl()));
            }
        }
        return dto;
    }

    private Delivery matchCompany(String companyName) {
        if (StrUtil.isBlank(companyName)) {
            return null;
        }
        List<Delivery> list = deliveryService.list();
        for (Delivery delivery : list) {
            if (StrUtil.isBlank(delivery.getDvyName())) {
                continue;
            }
            if (delivery.getDvyName().contains(companyName) || companyName.contains(delivery.getDvyName())) {
                return delivery;
            }
        }
        String code = ExpressCompanyCode.fromCompanyName(companyName);
        if (StrUtil.isBlank(code)) {
            return null;
        }
        for (Delivery delivery : list) {
            if (code.equals(ExpressCompanyCode.fromQueryUrl(delivery.getQueryUrl()))) {
                return delivery;
            }
        }
        return null;
    }

    private static String place(UserAddrOrder addr) {
        if (addr == null) {
            return "";
        }
        if (StrUtil.isNotBlank(addr.getCity())) {
            return addr.getCity();
        }
        return StrUtil.blankToDefault(addr.getProvince(), "");
    }
}
