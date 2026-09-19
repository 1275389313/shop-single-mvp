package com.yami.shop.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.shop.bean.app.param.OrderRefundAuditParam;
import com.yami.shop.bean.app.param.OrderRefundExpressParam;
import com.yami.shop.bean.app.param.OrderRefundParam;
import com.yami.shop.bean.app.param.OrderRefundReceiveParam;
import com.yami.shop.bean.enums.OrderStatus;
import com.yami.shop.bean.enums.RefundFlow;
import com.yami.shop.bean.model.Order;
import com.yami.shop.bean.model.OrderItem;
import com.yami.shop.bean.model.OrderRefund;
import com.yami.shop.bean.model.OrderSettlement;
import com.yami.shop.common.config.ShopMvpProperties;
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

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderSettlementMapper orderSettlementMapper;
    private final ShopMvpProperties shopMvpProperties;

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
                .in(OrderRefund::getRefundSts, RefundFlow.STS_PENDING, RefundFlow.STS_AGREE));
        if (pending > 0) {
            throw new YamiShopBindException("该订单已有退款申请");
        }

        Integer applyType = param.getApplyType() == null ? RefundFlow.APPLY_TYPE_REFUND_ONLY : param.getApplyType();
        if (!Objects.equals(applyType, RefundFlow.APPLY_TYPE_REFUND_ONLY)
                && !Objects.equals(applyType, RefundFlow.APPLY_TYPE_RETURN_GOODS)) {
            throw new YamiShopBindException("申请类型非法");
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
        refund.setApplyType(applyType);
        refund.setRefundSts(RefundFlow.STS_PENDING);
        refund.setReturnMoneySts(RefundFlow.MONEY_PROCESSING);
        refund.setApplyTime(now);
        refund.setPhotoFiles(param.getPhotoFiles());
        refund.setBuyerMsg(param.getBuyerMsg());
        save(refund);

        patchOrderRefundSts(order.getOrderId(), RefundFlow.ORDER_REFUND_PROCESSING);
        RefundFlow.fill(refund);
        log.info("refund applied orderNumber={} refundSn={} applyType={}", order.getOrderNumber(), refund.getRefundSn(), refund.getApplyType());
        return refund;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderRefund audit(Long shopId, OrderRefundAuditParam param) {
        OrderRefund refund = getOwnedRefund(shopId, param.getRefundId());
        if (!Objects.equals(refund.getRefundSts(), RefundFlow.STS_PENDING)) {
            throw new YamiShopBindException("退款单已审核");
        }
        if (!Objects.equals(param.getRefundSts(), RefundFlow.STS_AGREE) && !Objects.equals(param.getRefundSts(), RefundFlow.STS_REJECT)) {
            throw new YamiShopBindException("审核结果非法");
        }

        Date now = new Date();
        refund.setRefundSts(param.getRefundSts());
        refund.setSellerMsg(param.getSellerMsg());
        refund.setHandelTime(now);
        if (Objects.equals(param.getRefundSts(), RefundFlow.STS_REJECT)) {
            refund.setRejectMessage(param.getSellerMsg());
            refund.setReturnMoneySts(RefundFlow.MONEY_FAIL);
            updateById(refund);
            patchOrderRefundSts(refund.getOrderId(), 0);
        } else if (Objects.equals(refund.getApplyType(), RefundFlow.APPLY_TYPE_RETURN_GOODS)) {
            refund.setReturnMoneySts(RefundFlow.MONEY_PROCESSING);
            updateById(refund);
            // keep order refund processing until merchant confirms returned goods
            log.info("return-goods approved, waiting buyer ship refundSn={}", refund.getRefundSn());
        } else {
            completeMockRefund(refund, now);
            updateById(refund);
            patchOrderRefundSts(refund.getOrderId(), RefundFlow.ORDER_REFUND_DONE);
        }
        RefundFlow.fill(refund);
        return refund;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderRefund submitExpress(String userId, OrderRefundExpressParam param) {
        OrderRefund refund = getOne(new LambdaQueryWrapper<OrderRefund>()
                .eq(OrderRefund::getRefundSn, param.getRefundSn())
                .eq(OrderRefund::getUserId, userId)
                .last("limit 1"));
        if (refund == null) {
            throw new YamiShopBindException("退款单不存在");
        }
        if (!RefundFlow.canEditReturnExpress(refund)) {
            throw new YamiShopBindException("当前状态不可填写退货物流");
        }
        Date now = new Date();
        refund.setExpressName(param.getExpressName().trim());
        refund.setExpressNo(param.getExpressNo().trim());
        if (refund.getShipTime() == null) {
            refund.setShipTime(now);
        }
        updateById(refund);
        RefundFlow.fill(refund);
        log.info("return-goods express submitted refundSn={} expressNo={}", refund.getRefundSn(), refund.getExpressNo());
        return refund;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderRefund confirmReceive(Long shopId, OrderRefundReceiveParam param) {
        OrderRefund refund = getOwnedRefund(shopId, param.getRefundId());
        if (!RefundFlow.waitingMerchantReceive(refund)) {
            throw new YamiShopBindException("买家尚未寄回或已完成退款");
        }
        Date now = new Date();
        refund.setReceiveTime(now);
        refund.setReceiveMessage(param.getReceiveMessage());
        completeMockRefund(refund, now);
        updateById(refund);
        patchOrderRefundSts(refund.getOrderId(), RefundFlow.ORDER_REFUND_DONE);
        RefundFlow.fill(refund);
        return refund;
    }

    @Override
    public OrderRefund getShopRefund(Long shopId, Long refundId) {
        OrderRefund refund = getOwnedRefund(shopId, refundId);
        RefundFlow.fill(refund);
        return refund;
    }

    @Override
    public IPage<OrderRefund> pageByShop(Long shopId, Integer refundSts, String orderNumber, PageParam<OrderRefund> page) {
        IPage<OrderRefund> result = page(page, new LambdaQueryWrapper<OrderRefund>()
                .eq(OrderRefund::getShopId, shopId)
                .eq(refundSts != null, OrderRefund::getRefundSts, refundSts)
                .like(StrUtil.isNotBlank(orderNumber), OrderRefund::getOrderNumber, orderNumber)
                .orderByDesc(OrderRefund::getApplyTime));
        result.getRecords().forEach(RefundFlow::fill);
        return result;
    }

    @Override
    public IPage<OrderRefund> pageByUser(String userId, PageParam<OrderRefund> page) {
        IPage<OrderRefund> result = page(page, new LambdaQueryWrapper<OrderRefund>()
                .eq(OrderRefund::getUserId, userId)
                .orderByDesc(OrderRefund::getApplyTime));
        result.getRecords().forEach(RefundFlow::fill);
        return result;
    }

    @Override
    public OrderRefund getByOrderNumber(String userId, String orderNumber) {
        OrderRefund refund = getOne(new LambdaQueryWrapper<OrderRefund>()
                .eq(OrderRefund::getUserId, userId)
                .eq(OrderRefund::getOrderNumber, orderNumber)
                .orderByDesc(OrderRefund::getApplyTime)
                .last("limit 1"));
        RefundFlow.fill(refund);
        return refund;
    }

    private OrderRefund getOwnedRefund(Long shopId, Long refundId) {
        OrderRefund refund = getById(refundId);
        if (refund == null || !Objects.equals(refund.getShopId(), shopId)) {
            throw new YamiShopBindException("退款单不存在");
        }
        return refund;
    }

    private void patchOrderRefundSts(Long orderId, Integer refundSts) {
        Order patch = new Order();
        patch.setOrderId(orderId);
        patch.setRefundSts(refundSts);
        orderMapper.updateById(patch);
    }

    /**
     * Marks money success in DB. Does not call WeChat refund (open-source mall4j has no live WxPay refund).
     */
    private void completeMockRefund(OrderRefund refund, Date now) {
        refund.setReturnMoneySts(RefundFlow.MONEY_SUCCESS);
        refund.setRefundTime(now);
        if (shopMvpProperties.getMock().isPay()) {
            log.info("mock refund completed refundSn={} amount={} (no WeChat refund API)",
                    refund.getRefundSn(), refund.getRefundAmount());
        } else {
            log.warn("TODO WeChat refund API not implemented; DB marked success refundSn={} amount={}",
                    refund.getRefundSn(), refund.getRefundAmount());
        }
    }
}
