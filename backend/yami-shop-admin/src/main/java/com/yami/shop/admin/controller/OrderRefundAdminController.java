package com.yami.shop.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.bean.app.param.OrderRefundAuditParam;
import com.yami.shop.bean.app.param.OrderRefundReceiveParam;
import com.yami.shop.bean.app.dto.DeliveryDto;
import com.yami.shop.bean.model.OrderRefund;
import com.yami.shop.common.annotation.SysLog;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.security.admin.util.SecurityUtils;
import com.yami.shop.service.DeliveryTrackingService;
import com.yami.shop.service.OrderRefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin refund audit + confirm returned goods. Wired to mall4v 退款审核 page.
 */
@RestController
@RequestMapping("/order/refund")
@RequiredArgsConstructor
public class OrderRefundAdminController {

    private final OrderRefundService orderRefundService;
    private final DeliveryTrackingService deliveryTrackingService;

    @GetMapping("/page")
    @PreAuthorize("@pms.hasPermission('order:refund:page')")
    public ServerResponseEntity<IPage<OrderRefund>> page(Integer refundSts, String orderNumber, PageParam<OrderRefund> page) {
        Long shopId = SecurityUtils.getSysUser().getShopId();
        return ServerResponseEntity.success(orderRefundService.pageByShop(shopId, refundSts, orderNumber, page));
    }

    @PutMapping("/audit")
    @SysLog("退款审核")
    @PreAuthorize("@pms.hasPermission('order:refund:audit')")
    public ServerResponseEntity<OrderRefund> audit(@Valid @RequestBody OrderRefundAuditParam param) {
        Long shopId = SecurityUtils.getSysUser().getShopId();
        return ServerResponseEntity.success(orderRefundService.audit(shopId, param));
    }

    @PutMapping("/receive")
    @SysLog("确认退货并退款")
    @PreAuthorize("@pms.hasPermission('order:refund:receive') or @pms.hasPermission('order:refund:audit')")
    public ServerResponseEntity<OrderRefund> receive(@Valid @RequestBody OrderRefundReceiveParam param) {
        Long shopId = SecurityUtils.getSysUser().getShopId();
        return ServerResponseEntity.success(orderRefundService.confirmReceive(shopId, param));
    }

    @GetMapping("/info")
    @PreAuthorize("@pms.hasPermission('order:refund:info')")
    public ServerResponseEntity<OrderRefund> info(@RequestParam Long refundId) {
        Long shopId = SecurityUtils.getSysUser().getShopId();
        return ServerResponseEntity.success(orderRefundService.getShopRefund(shopId, refundId));
    }

    @GetMapping("/delivery")
    @PreAuthorize("@pms.hasPermission('order:refund:info')")
    public ServerResponseEntity<DeliveryDto> delivery(@RequestParam Long refundId) {
        Long shopId = SecurityUtils.getSysUser().getShopId();
        return ServerResponseEntity.success(deliveryTrackingService.queryReturnForShop(shopId, refundId));
    }
}
