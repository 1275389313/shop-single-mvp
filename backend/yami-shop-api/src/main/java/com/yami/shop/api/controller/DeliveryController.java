package com.yami.shop.api.controller;

import cn.hutool.core.util.StrUtil;
import com.yami.shop.bean.app.dto.DeliveryDto;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.response.ResponseEnum;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.security.common.bo.UserInfoInTokenBO;
import com.yami.shop.security.common.util.AuthUserContext;
import com.yami.shop.service.DeliveryTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Reuses mall4j {@code /delivery/check}. Also mounted at {@code /p/delivery} so login is required.
 */
@RestController
@RequestMapping({"/delivery", "/p/delivery"})
@Tag(name = "查看物流接口")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryTrackingService deliveryTrackingService;

    @GetMapping("/check")
    @Operation(summary = "查看发货物流", description = "根据订单号查看正向物流轨迹。无快递100密钥时返回模拟轨迹。")
    @Parameter(name = "orderNumber", description = "订单号", required = true)
    public ServerResponseEntity<DeliveryDto> checkDelivery(@RequestParam("orderNumber") String orderNumber) {
        if (StrUtil.isBlank(orderNumber)) {
            throw new YamiShopBindException("订单号不能为空");
        }
        return ServerResponseEntity.success(deliveryTrackingService.queryShipmentForUser(currentUserId(), orderNumber));
    }

    private static String currentUserId() {
        UserInfoInTokenBO user = AuthUserContext.get();
        if (user == null || StrUtil.isBlank(user.getUserId())) {
            throw new YamiShopBindException(ResponseEnum.UNAUTHORIZED);
        }
        return user.getUserId();
    }
}
