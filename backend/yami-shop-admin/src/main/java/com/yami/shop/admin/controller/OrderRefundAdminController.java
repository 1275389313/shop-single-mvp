package com.yami.shop.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.bean.app.param.OrderRefundAuditParam;
import com.yami.shop.bean.model.OrderRefund;
import com.yami.shop.common.annotation.SysLog;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.security.admin.util.SecurityUtils;
import com.yami.shop.service.OrderRefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin refund audit. Uni-app / mall4v UI for this screen is still a P0 gap.
 */
@RestController
@RequestMapping("/order/refund")
@RequiredArgsConstructor
public class OrderRefundAdminController {

    private final OrderRefundService orderRefundService;

    @GetMapping("/page")
    public ServerResponseEntity<IPage<OrderRefund>> page(Integer refundSts, PageParam<OrderRefund> page) {
        Long shopId = SecurityUtils.getSysUser().getShopId();
        return ServerResponseEntity.success(orderRefundService.pageByShop(shopId, refundSts, page));
    }

    @PutMapping("/audit")
    @SysLog("退款审核")
    public ServerResponseEntity<OrderRefund> audit(@Valid @RequestBody OrderRefundAuditParam param) {
        Long shopId = SecurityUtils.getSysUser().getShopId();
        return ServerResponseEntity.success(orderRefundService.audit(shopId, param));
    }

    @GetMapping("/info")
    public ServerResponseEntity<OrderRefund> info(@RequestParam Long refundId) {
        return ServerResponseEntity.success(orderRefundService.getById(refundId));
    }
}
