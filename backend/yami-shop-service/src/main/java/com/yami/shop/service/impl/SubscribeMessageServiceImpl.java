package com.yami.shop.service.impl;

import cn.hutool.core.util.StrUtil;
import com.yami.shop.bean.dto.SubscribeMessageConfigDto;
import com.yami.shop.bean.model.Delivery;
import com.yami.shop.bean.model.Order;
import com.yami.shop.bean.model.User;
import com.yami.shop.bean.util.SubscribeMessageRules;
import com.yami.shop.common.config.ShopMvpProperties;
import com.yami.shop.dao.SysParamMapper;
import com.yami.shop.service.DeliveryService;
import com.yami.shop.service.SubscribeMessageService;
import com.yami.shop.service.UserService;
import com.yami.shop.service.wx.WeChatSubscribeClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Pay/ship subscribe hooks. Default local MVP has empty template IDs and no AppSecret → log skip.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscribeMessageServiceImpl implements SubscribeMessageService {

    private final ShopMvpProperties shopMvpProperties;
    private final SysParamMapper sysParamMapper;
    private final UserService userService;
    private final DeliveryService deliveryService;
    private final WeChatSubscribeClient weChatSubscribeClient;

    @Override
    public void notifyPaySuccess(Order order) {
        send(SubscribeMessageRules.Scene.PAY_SUCCESS, order);
    }

    @Override
    public void notifyShipment(Order order) {
        send(SubscribeMessageRules.Scene.SHIPMENT, order);
    }

    @Override
    public SubscribeMessageConfigDto publicConfig() {
        SubscribeMessageConfigDto dto = new SubscribeMessageConfigDto();
        dto.setPaySuccessTemplateId(resolveTemplate(SubscribeMessageRules.Scene.PAY_SUCCESS));
        dto.setShipTemplateId(resolveTemplate(SubscribeMessageRules.Scene.SHIPMENT));
        return dto;
    }

    private void send(SubscribeMessageRules.Scene scene, Order order) {
        try {
            String templateId = resolveTemplate(scene);
            ShopMvpProperties.Wx wx = shopMvpProperties.getWx() == null
                    ? new ShopMvpProperties.Wx() : shopMvpProperties.getWx();
            String openId = resolveOpenId(order);
            SubscribeMessageRules.Decision decision = SubscribeMessageRules.decide(
                    templateId, wx.getAppId(), wx.getAppSecret(), openId, order);
            String orderNumber = order == null ? null : order.getOrderNumber();
            if (decision != SubscribeMessageRules.Decision.SEND) {
                log.info(SubscribeMessageRules.skipLog(decision, scene, orderNumber));
                return;
            }
            String page = SubscribeMessageRules.orderDetailPage(orderNumber);
            String company = resolveCompanyName(order);
            var data = scene == SubscribeMessageRules.Scene.PAY_SUCCESS
                    ? SubscribeMessageRules.payData(order)
                    : SubscribeMessageRules.shipData(order, company);
            weChatSubscribeClient.send(
                    wx.getAppId(),
                    wx.getAppSecret(),
                    openId.trim(),
                    templateId,
                    page,
                    wx.getSubscribeMiniprogramState(),
                    data);
        } catch (Exception ex) {
            log.warn("subscribe {} hook failed order={} err={}",
                    scene, order == null ? null : order.getOrderNumber(), ex.getClass().getSimpleName());
        }
    }

    private String resolveTemplate(SubscribeMessageRules.Scene scene) {
        ShopMvpProperties.Wx wx = shopMvpProperties.getWx() == null
                ? new ShopMvpProperties.Wx() : shopMvpProperties.getWx();
        String key = scene == SubscribeMessageRules.Scene.PAY_SUCCESS
                ? SubscribeMessageRules.PAY_TEMPLATE_KEY : SubscribeMessageRules.SHIP_TEMPLATE_KEY;
        String fromSys = null;
        try {
            fromSys = sysParamMapper.getValue(key);
        } catch (Exception ex) {
            log.info("subscribe template sys_config unread key={} (table may be missing)", key);
        }
        String fromEnv = scene == SubscribeMessageRules.Scene.PAY_SUCCESS
                ? wx.getSubscribePayTemplateId() : wx.getSubscribeShipTemplateId();
        return SubscribeMessageRules.normalizeTemplateId(SubscribeMessageRules.firstNonBlank(fromSys, fromEnv));
    }

    private String resolveOpenId(Order order) {
        if (order == null || StrUtil.isBlank(order.getUserId())) {
            return "";
        }
        User user = userService.getUserByUserId(order.getUserId());
        if (user == null) {
            return "";
        }
        return StrUtil.blankToDefault(user.getWxOpenId(), "");
    }

    private String resolveCompanyName(Order order) {
        if (order == null || order.getDvyId() == null) {
            return "";
        }
        try {
            Delivery delivery = deliveryService.getById(order.getDvyId());
            return delivery == null ? "" : StrUtil.blankToDefault(delivery.getDvyName(), "");
        } catch (Exception ex) {
            return "";
        }
    }
}
