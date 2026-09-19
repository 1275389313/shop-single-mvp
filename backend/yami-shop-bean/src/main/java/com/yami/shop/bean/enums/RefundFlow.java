package com.yami.shop.bean.enums;

import cn.hutool.core.util.StrUtil;
import com.yami.shop.bean.model.OrderRefund;

import java.util.Objects;

/**
 * After-sales display flow. Audit column {@code refundSts} stays 1/2/3;
 * return-goods waiting states are derived from express / money fields.
 */
public final class RefundFlow {

    private RefundFlow() {
    }

    public static final int APPLY_TYPE_REFUND_ONLY = 1;
    public static final int APPLY_TYPE_RETURN_GOODS = 2;

    public static final int STS_PENDING = 1;
    public static final int STS_AGREE = 2;
    public static final int STS_REJECT = 3;

    public static final int MONEY_PROCESSING = 0;
    public static final int MONEY_SUCCESS = 1;
    public static final int MONEY_FAIL = -1;

    public static final int ORDER_REFUND_PROCESSING = 1;
    public static final int ORDER_REFUND_DONE = 2;

    public static final String WAIT_AUDIT = "WAIT_AUDIT";
    public static final String WAIT_SHIP = "WAIT_SHIP";
    public static final String WAIT_RECEIVE = "WAIT_RECEIVE";
    public static final String REFUNDED = "REFUNDED";
    public static final String REJECTED = "REJECTED";

    public static void fill(OrderRefund refund) {
        if (refund == null) {
            return;
        }
        String code = codeOf(refund);
        refund.setFlowCode(code);
        refund.setFlowText(textOf(code));
    }

    public static String codeOf(OrderRefund refund) {
        if (refund == null) {
            return null;
        }
        if (Objects.equals(refund.getRefundSts(), STS_PENDING)) {
            return WAIT_AUDIT;
        }
        if (Objects.equals(refund.getRefundSts(), STS_REJECT)) {
            return REJECTED;
        }
        if (Objects.equals(refund.getApplyType(), APPLY_TYPE_RETURN_GOODS)
                && !Objects.equals(refund.getReturnMoneySts(), MONEY_SUCCESS)) {
            if (StrUtil.isBlank(refund.getExpressNo())) {
                return WAIT_SHIP;
            }
            return WAIT_RECEIVE;
        }
        if (Objects.equals(refund.getReturnMoneySts(), MONEY_SUCCESS)
                || Objects.equals(refund.getRefundSts(), STS_AGREE)) {
            return REFUNDED;
        }
        return WAIT_AUDIT;
    }

    public static String textOf(String code) {
        if (code == null) {
            return "";
        }
        return switch (code) {
            case WAIT_AUDIT -> "待商家审核";
            case WAIT_SHIP -> "请寄回商品";
            case WAIT_RECEIVE -> "待商家收货";
            case REFUNDED -> "退款成功";
            case REJECTED -> "商家已拒绝";
            default -> "未知";
        };
    }

    public static boolean waitingBuyerShip(OrderRefund refund) {
        return WAIT_SHIP.equals(codeOf(refund));
    }

    public static boolean waitingMerchantReceive(OrderRefund refund) {
        return WAIT_RECEIVE.equals(codeOf(refund));
    }

    public static boolean canEditReturnExpress(OrderRefund refund) {
        return waitingBuyerShip(refund) || waitingMerchantReceive(refund);
    }
}
