package com.yami.shop.api.controller;

import com.yami.shop.bean.app.param.MockPayNoticeParam;
import com.yami.shop.common.config.ShopMvpProperties;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.service.PayService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Pay callbacks. /notice/pay is not under /p/*, so AuthFilter does not require login.
 */
@Slf4j
@Hidden
@RestController
@RequestMapping("/notice/pay")
@RequiredArgsConstructor
public class PayNoticeController {

    private final PayService payService;
    private final ShopMvpProperties shopMvpProperties;

    @PostMapping("/mock")
    @Operation(summary = "Mock WeChat pay callback (idempotent)")
    public ServerResponseEntity<Void> mock(@Valid @RequestBody MockPayNoticeParam param) {
        if (!shopMvpProperties.getMock().isPay()) {
            throw new YamiShopBindException("shop-mvp.mock.pay=false，拒绝 mock 回调");
        }
        String bizPayNo = param.getBizPayNo() == null || param.getBizPayNo().isBlank()
                ? "MOCK-" + param.getPayNo()
                : param.getBizPayNo();
        payService.paySuccess(param.getPayNo(), bizPayNo);
        return ServerResponseEntity.success();
    }

    @PostMapping("/wechat")
    @Operation(summary = "Real WeChat pay XML notify — not implemented")
    public String wechat(@RequestBody String xmlData) {
        // TODO: verify WeChat pay signature, parse out_trade_no / transaction_id, then paySuccess()
        log.warn("real WeChat pay notify received but not implemented, bodyLength={}", xmlData == null ? 0 : xmlData.length());
        return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[NOT_IMPLEMENTED]]></return_msg></xml>";
    }
}
