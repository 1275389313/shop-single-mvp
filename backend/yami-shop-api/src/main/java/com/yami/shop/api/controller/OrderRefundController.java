package com.yami.shop.api.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.bean.app.param.OrderRefundExpressParam;
import com.yami.shop.bean.app.param.OrderRefundParam;
import com.yami.shop.bean.model.Delivery;
import com.yami.shop.bean.model.OrderRefund;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.service.DeliveryService;
import com.yami.shop.service.OrderRefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/p/refund")
@Tag(name = "用户退款")
@RequiredArgsConstructor
public class OrderRefundController {

    private final OrderRefundService orderRefundService;
    private final DeliveryService deliveryService;

    @PostMapping("/apply")
    @Operation(summary = "申请退款/退货退款（审核走后台）")
    public ServerResponseEntity<OrderRefund> apply(@Valid @RequestBody OrderRefundParam param) {
        String userId = SecurityUtils.getUser().getUserId();
        return ServerResponseEntity.success(orderRefundService.apply(userId, param));
    }

    @PutMapping("/express")
    @Operation(summary = "退货退款：审核同意后填写/修改退货物流")
    public ServerResponseEntity<OrderRefund> express(@Valid @RequestBody OrderRefundExpressParam param) {
        String userId = SecurityUtils.getUser().getUserId();
        return ServerResponseEntity.success(orderRefundService.submitExpress(userId, param));
    }

    @GetMapping("/deliveryList")
    @Operation(summary = "退货可选物流公司名称（来自 tz_delivery，不含查询密钥）")
    public ServerResponseEntity<List<String>> deliveryList() {
        List<String> names = deliveryService.list().stream()
                .map(Delivery::getDvyName)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .toList();
        return ServerResponseEntity.success(names);
    }

    @GetMapping("/page")
    @Operation(summary = "我的退款列表")
    public ServerResponseEntity<IPage<OrderRefund>> page(PageParam<OrderRefund> page) {
        String userId = SecurityUtils.getUser().getUserId();
        return ServerResponseEntity.success(orderRefundService.pageByUser(userId, page));
    }

    @GetMapping("/byOrder")
    @Operation(summary = "按订单号查询最近一笔退款（无则 data=null）")
    public ServerResponseEntity<OrderRefund> byOrder(@RequestParam String orderNumber) {
        String userId = SecurityUtils.getUser().getUserId();
        return ServerResponseEntity.success(orderRefundService.getByOrderNumber(userId, orderNumber));
    }
}
