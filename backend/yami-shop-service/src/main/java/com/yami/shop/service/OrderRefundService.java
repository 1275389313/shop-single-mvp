package com.yami.shop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.shop.bean.app.param.OrderRefundAuditParam;
import com.yami.shop.bean.app.param.OrderRefundParam;
import com.yami.shop.bean.model.OrderRefund;
import com.yami.shop.common.util.PageParam;

/**
 * User refund apply + admin audit hooks.
 * Real WeChat refund is TODO when mock pay is turned off.
 */
public interface OrderRefundService extends IService<OrderRefund> {

    OrderRefund apply(String userId, OrderRefundParam param);

    OrderRefund audit(Long shopId, OrderRefundAuditParam param);

    IPage<OrderRefund> pageByShop(Long shopId, Integer refundSts, String orderNumber, PageParam<OrderRefund> page);

    IPage<OrderRefund> pageByUser(String userId, PageParam<OrderRefund> page);

    OrderRefund getByOrderNumber(String userId, String orderNumber);
}
