package com.yami.shop.api.controller;

import com.yami.shop.bean.app.param.PayParam;
import com.yami.shop.bean.pay.PayInfoDto;
import com.yami.shop.bean.pay.PayResultDto;
import com.yami.shop.common.config.ShopMvpProperties;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.security.api.model.YamiUser;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.service.PayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Payment entry. Default is mock (immediate paySuccess). Real WeChat prepay is TODO.
 */
@RestController
@RequestMapping("/p/order")
@Tag(name = "订单支付")
@RequiredArgsConstructor
public class PayController {

    private final PayService payService;
    private final ShopMvpProperties shopMvpProperties;

    @PostMapping("/pay")
    @Operation(summary = "根据订单号进行支付")
    public ServerResponseEntity<PayResultDto> pay(@RequestBody PayParam payParam) {
        return ServerResponseEntity.success(doPay(payParam));
    }

    @PostMapping("/normalPay")
    @Operation(summary = "根据订单号进行支付（uni-app 默认走此接口）")
    public ServerResponseEntity<PayResultDto> normalPay(@RequestBody PayParam payParam) {
        return ServerResponseEntity.success(doPay(payParam));
    }

    private PayResultDto doPay(PayParam payParam) {
        YamiUser user = SecurityUtils.getUser();
        if (payParam.getPayType() == null) {
            payParam.setPayType(1);
        }
        PayInfoDto payInfo = payService.pay(user.getUserId(), payParam);
        PayResultDto result = new PayResultDto();
        result.setPayNo(payInfo.getPayNo());
        result.setMock(shopMvpProperties.getMock().isPay());
        if (shopMvpProperties.getMock().isPay()) {
            payService.paySuccess(payInfo.getPayNo(), "MOCK-" + payInfo.getPayNo());
            result.setPaid(true);
        } else {
            // TODO: create WeChat prepay order with mchId/apiKey and return paySign fields
            result.setPaid(false);
        }
        return result;
    }
}
