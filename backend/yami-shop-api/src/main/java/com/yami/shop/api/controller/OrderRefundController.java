package com.yami.shop.api.controller;

import com.yami.shop.bean.app.param.OrderRefundParam;
import com.yami.shop.bean.model.OrderRefund;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.service.OrderRefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/p/refund")
@Tag(name = "用户退款")
@RequiredArgsConstructor
public class OrderRefundController {

    private final OrderRefundService orderRefundService;

    @PostMapping("/apply")
    @Operation(summary = "申请退款/退货退款（审核走后台）")
    public ServerResponseEntity<OrderRefund> apply(@Valid @RequestBody OrderRefundParam param) {
        String userId = SecurityUtils.getUser().getUserId();
        return ServerResponseEntity.success(orderRefundService.apply(userId, param));
    }
}
