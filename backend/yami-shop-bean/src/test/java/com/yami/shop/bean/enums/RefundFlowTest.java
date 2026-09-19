package com.yami.shop.bean.enums;

import com.yami.shop.bean.model.OrderRefund;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RefundFlowTest {

    @Test
    void pendingIsWaitAudit() {
        OrderRefund refund = base();
        refund.setRefundSts(RefundFlow.STS_PENDING);
        RefundFlow.fill(refund);
        assertEquals(RefundFlow.WAIT_AUDIT, refund.getFlowCode());
        assertEquals("待商家审核", refund.getFlowText());
    }

    @Test
    void refundOnlyAgreeIsRefunded() {
        OrderRefund refund = base();
        refund.setApplyType(RefundFlow.APPLY_TYPE_REFUND_ONLY);
        refund.setRefundSts(RefundFlow.STS_AGREE);
        refund.setReturnMoneySts(RefundFlow.MONEY_SUCCESS);
        RefundFlow.fill(refund);
        assertEquals(RefundFlow.REFUNDED, refund.getFlowCode());
    }

    @Test
    void returnGoodsAgreeWithoutExpressIsWaitShip() {
        OrderRefund refund = base();
        refund.setApplyType(RefundFlow.APPLY_TYPE_RETURN_GOODS);
        refund.setRefundSts(RefundFlow.STS_AGREE);
        refund.setReturnMoneySts(RefundFlow.MONEY_PROCESSING);
        RefundFlow.fill(refund);
        assertEquals(RefundFlow.WAIT_SHIP, refund.getFlowCode());
        assertTrue(RefundFlow.canEditReturnExpress(refund));
        assertFalse(RefundFlow.waitingMerchantReceive(refund));
    }

    @Test
    void returnGoodsShippedIsWaitReceive() {
        OrderRefund refund = base();
        refund.setApplyType(RefundFlow.APPLY_TYPE_RETURN_GOODS);
        refund.setRefundSts(RefundFlow.STS_AGREE);
        refund.setReturnMoneySts(RefundFlow.MONEY_PROCESSING);
        refund.setExpressName("顺丰");
        refund.setExpressNo("SF123");
        RefundFlow.fill(refund);
        assertEquals(RefundFlow.WAIT_RECEIVE, refund.getFlowCode());
        assertEquals("待商家收货", refund.getFlowText());
        assertTrue(RefundFlow.waitingMerchantReceive(refund));
        assertTrue(RefundFlow.canEditReturnExpress(refund));
    }

    @Test
    void returnGoodsCompletedIsRefunded() {
        OrderRefund refund = base();
        refund.setApplyType(RefundFlow.APPLY_TYPE_RETURN_GOODS);
        refund.setRefundSts(RefundFlow.STS_AGREE);
        refund.setReturnMoneySts(RefundFlow.MONEY_SUCCESS);
        refund.setExpressNo("SF123");
        RefundFlow.fill(refund);
        assertEquals(RefundFlow.REFUNDED, refund.getFlowCode());
        assertFalse(RefundFlow.canEditReturnExpress(refund));
    }

    @Test
    void rejected() {
        OrderRefund refund = base();
        refund.setRefundSts(RefundFlow.STS_REJECT);
        refund.setReturnMoneySts(RefundFlow.MONEY_FAIL);
        RefundFlow.fill(refund);
        assertEquals(RefundFlow.REJECTED, refund.getFlowCode());
    }

    private static OrderRefund base() {
        OrderRefund refund = new OrderRefund();
        refund.setApplyType(RefundFlow.APPLY_TYPE_REFUND_ONLY);
        return refund;
    }
}
