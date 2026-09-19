package com.yami.shop.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.shop.bean.app.param.OrderRefundAuditParam;
import com.yami.shop.bean.app.param.OrderRefundParam;
import com.yami.shop.bean.enums.OrderStatus;
import com.yami.shop.bean.model.Order;
import com.yami.shop.bean.model.OrderItem;
import com.yami.shop.bean.model.OrderRefund;
import com.yami.shop.bean.model.OrderSettlement;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.dao.OrderItemMapper;
import com.yami.shop.dao.OrderMapper;
import com.yami.shop.dao.OrderRefundMapper;
import com.yami.shop.dao.OrderSettlementMapper;
import com.yami.shop.service.OrderRefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderRefundServiceImpl extends ServiceImpl<OrderRefundMapper, OrderRefund> implements OrderRefundService {

    private static final int APPLY_TYPE_REFUND_ONLY = 1;
    private static final int APPLY_TYPE_RETURN_GOODS = 2;
    private static final int REFUND_STS_PENDING = 1;
    private static final int REFUND_STS_AGREE = 2;
    private static final int REFUND_STS_REJECT = 3;
    private static final int RETURN_MONEY_PROCESSING = 0;
    private static final int RETURN_MONEY_SUCCESS = 1;
    private static final int ORDER_REFUND_PROCESSING = 1;
    private static final int ORDER_REFUND_DONE = 2;

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderSettlementMapper orderSettlementMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderRefund apply(String userId, OrderRefundParam param) {
        Order order = orderMapper.getOrderByOrderNumber(param.getOrderNumber());
        if (order == null || !Objects.equals(order.getUserId(), userId)) {
            throw new YamiShopBindException("订单不存在");
        }
        Integer status = order.getStatus();
        if (Objects.equals(status, OrderStatus.UNPAY.value()) || Objects.equals(status, OrderStatus.CLOSE.value())) {
            throw new YamiShopBindException("当前订单状态不可申请退款");
        }
        long pending = count(new LambdaQueryWrapper<OrderRefund>()
                .eq(OrderRefund::getOrderNumber, param.getOrderNumber())
                .in(OrderRefund::getRefundSts, REFUND_STS_PENDING, REFUND_STS_AGREE));
        if (pending > 0) {
            throw new YamiShopBindException("该订单已有退款申请");
        }

        double refundAmount = order.getActualTotal() == null ? 0D : order.getActualTotal();
        int goodsNum = order.getProductNums() == null ? 0 : order.getProductNums();
        if (param.getOrderItemId() != null && param.getOrderItemId() > 0) {
            OrderItem item = orderItemMapper.selectById(param.getOrderItemId());
            if (item == null || !Objects.equals(item.getOrderNumber(), order.getOrderNumber())) {
                throw new YamiShopBindException("订单项不存在");
            }
            refundAmount = item.getProductTotalAmount() == null ? 0D : item.getProductTotalAmount();
            goodsNum = item.getProdCount() == null ? 0 : item.getProdCount();
        }

        OrderSettlement settlement = orderSettlementMapper.selectOne(new LambdaQueryWrapper<OrderSettlement>()
                .eq(OrderSettlement::getOrderNumber, order.getOrderNumber())
                .last("limit 1"));

        Date now = new Date();
        OrderRefund refund = new OrderRefund();
        refund.setShopId(order.getShopId());
        refund.setOrderId(order.getOrderId());
        refund.setOrderNumber(order.getOrderNumber());
        refund.setOrderAmount(order.getActualTotal());
        refund.setOrderItemId(param.getOrderItemId() == null ? 0L : param.getOrderItemId());
        refund.setRefundSn("R" + IdUtil.getSnowflakeNextIdStr());
        refund.setFlowTradeNo(settlement == null || StrUtil.isBlank(settlement.getPayNo()) ? order.getOrderNumber() : settlement.getPayNo());
        refund.setPayType(order.getPayType());
        refund.setPayTypeName("微信支付");
        refund.setUserId(userId);
        refund.setGoodsNum(goodsNum);
        refund.setRefundAmount(refundAmount);
        refund.setApplyType(param.getApplyType() == null ? APPLY_TYPE_REFUND_ONLY : param.getApplyType());
        refund.setRefundSts(REFUND_STS_PENDING);
        refund.setReturnMoneySts(RETURN_MONEY_PROCESSING);
        refund.setApplyTime(now);
        refund.setPhotoFiles(param.getPhotoFiles());
        refund.setBuyerMsg(param.getBuyerMsg());
        save(refund);

        Order patch = new Order();
        patch.setOrderId(order.getOrderId());
        patch.setRefundSts(ORDER_REFUND_PROCESSING);
        orderMapper.updateById(patch);

        log.info("refund applied orderNumber={} refundSn={} applyType={}", order.getOrderNumber(), refund.getRefundSn(), refund.getApplyType());
        if (Objects.equals(refund.getApplyType(), APPLY_TYPE_RETURN_GOODS)) {
            // TODO: buyer return-goods logistics (OrderRefundExpressParam) is not wired in uni-app yet
            log.warn("TODO refund return-goods: uni-app has no express submit UI, API param exists: OrderRefundExpressParam");
        }
        return refund;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderRefund audit(Long shopId, OrderRefundAuditParam param) {
        OrderRefund refund = getById(param.getRefundId());
        if (refund == null || !Objects.equals(refund.getShopId(), shopId)) {
            throw new YamiShopBindException("退款单不存在");
        }
        if (!Objects.equals(refund.getRefundSts(), REFUND_STS_PENDING)) {
            throw new YamiShopBindException("退款单已审核");
        }
        if (!Objects.equals(param.getRefundSts(), REFUND_STS_AGREE) && !Objects.equals(param.getRefundSts(), REFUND_STS_REJECT)) {
            throw new YamiShopBindException("审核结果非法");
        }

        Date now = new Date();
        refund.setRefundSts(param.getRefundSts());
        refund.setSellerMsg(param.getSellerMsg());
        refund.setHandelTime(now);
        if (Objects.equals(param.getRefundSts(), REFUND_STS_REJECT)) {
            refund.setRejectMessage(param.getSellerMsg());
            refund.setReturnMoneySts(-1);
        } else {
            refund.setReturnMoneySts(RETURN_MONEY_SUCCESS);
            refund.setRefundTime(now);
            // TODO: call WeChat refund API when shop-mvp.mock.pay=false
            log.info("mock refund approved refundSn={} amount={} (no WeChat refund API in open-source mall4j)",
                    refund.getRefundSn(), refund.getRefundAmount());
        }
        updateById(refund);

        Order patch = new Order();
        patch.setOrderId(refund.getOrderId());
        patch.setRefundSts(Objects.equals(param.getRefundSts(), REFUND_STS_AGREE) ? ORDER_REFUND_DONE : 0);
        orderMapper.updateById(patch);
        return refund;
    }

    @Override
    public IPage<OrderRefund> pageByShop(Long shopId, Integer refundSts, PageParam<OrderRefund> page) {
        return page(page, new LambdaQueryWrapper<OrderRefund>()
                .eq(OrderRefund::getShopId, shopId)
                .eq(refundSts != null, OrderRefund::getRefundSts, refundSts)
                .orderByDesc(OrderRefund::getApplyTime));
    }
}
