package com.yami.shop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.shop.bean.app.param.OrderRefundAuditParam;
import com.yami.shop.bean.app.param.OrderRefundExpressParam;
import com.yami.shop.bean.app.param.OrderRefundParam;
import com.yami.shop.bean.app.param.OrderRefundReceiveParam;
import com.yami.shop.bean.model.OrderRefund;
import com.yami.shop.common.util.PageParam;

/**
 * User refund apply + return-goods logistics + admin audit/receive.
 * Real WeChat refund is TODO when mock pay is turned off.
 */
public interface OrderRefundService extends IService<OrderRefund> {

    OrderRefund apply(String userId, OrderRefundParam param);

    OrderRefund audit(Long shopId, OrderRefundAuditParam param);

    OrderRefund submitExpress(String userId, OrderRefundExpressParam param);

    OrderRefund confirmReceive(Long shopId, OrderRefundReceiveParam param);

    OrderRefund getShopRefund(Long shopId, Long refundId);

    IPage<OrderRefund> pageByShop(Long shopId, Integer refundSts, String orderNumber, PageParam<OrderRefund> page);

    IPage<OrderRefund> pageByUser(String userId, PageParam<OrderRefund> page);

    OrderRefund getByOrderNumber(String userId, String orderNumber);
}
