package com.yami.shop.bean.util;

import com.yami.shop.bean.model.Order;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubscribeMessageRulesTest {

    @Test
    void unsetTemplateSkipsWithoutCredentials() {
        Order order = paidOrder();
        assertEquals(SubscribeMessageRules.Decision.SKIP_MISSING_TEMPLATE,
                SubscribeMessageRules.decide("", "app", "secret", "oREAL", order));
        assertEquals(SubscribeMessageRules.Decision.SKIP_MISSING_TEMPLATE,
                SubscribeMessageRules.decide("YOUR_TEMPLATE_ID", "", "", "oREAL", order));
        assertEquals(SubscribeMessageRules.Decision.SKIP_MISSING_TEMPLATE,
                SubscribeMessageRules.decide("-", "app", "secret", "oREAL", order));
        assertEquals(SubscribeMessageRules.Decision.SKIP_MISSING_TEMPLATE,
                SubscribeMessageRules.decide(null, null, null, null, order));
    }

    @Test
    void missingSecretSkipsWhenTemplatePresent() {
        Order order = paidOrder();
        assertEquals(SubscribeMessageRules.Decision.SKIP_MISSING_CREDENTIALS,
                SubscribeMessageRules.decide("tpl_pay_ok_12345678", "wx123", "", "oREALOPENID", order));
        assertEquals(SubscribeMessageRules.Decision.SKIP_MISSING_CREDENTIALS,
                SubscribeMessageRules.decide("tpl_pay_ok_12345678", "", "secret", "oREALOPENID", order));
    }

    @Test
    void mockOpenIdNeverSends() {
        Order order = paidOrder();
        assertEquals(SubscribeMessageRules.Decision.SKIP_MOCK_OPENID,
                SubscribeMessageRules.decide("tpl_pay_ok_12345678", "wx123", "secret", "mock_devtools-demo", order));
        assertTrue(SubscribeMessageRules.isMockOpenId("mock_abc"));
        assertFalse(SubscribeMessageRules.isMockOpenId("o6_bmjrPTlm6_2sgVt7hMZOPfL2M"));
        assertFalse(SubscribeMessageRules.isMockOpenId(""));
    }

    @Test
    void missingOpenIdOrOrderSkips() {
        Order order = paidOrder();
        assertEquals(SubscribeMessageRules.Decision.SKIP_MISSING_OPENID,
                SubscribeMessageRules.decide("tpl_pay_ok_12345678", "wx123", "secret", "  ", order));
        assertEquals(SubscribeMessageRules.Decision.SKIP_NO_ORDER,
                SubscribeMessageRules.decide("tpl_pay_ok_12345678", "wx123", "secret", "oREAL", null));
        Order blank = new Order();
        assertEquals(SubscribeMessageRules.Decision.SKIP_NO_ORDER,
                SubscribeMessageRules.decide("tpl_pay_ok_12345678", "wx123", "secret", "oREAL", blank));
    }

    @Test
    void readyToSendWhenTemplateCredentialsAndRealOpenIdPresent() {
        assertEquals(SubscribeMessageRules.Decision.SEND,
                SubscribeMessageRules.decide("tpl_pay_ok_12345678", "wx123", "secret", "oREALOPENID", paidOrder()));
    }

    @Test
    void sysConfigOverridesEnvWhenBothSet() {
        assertEquals("from-sys", SubscribeMessageRules.firstNonBlank("from-sys", "from-env"));
        assertEquals("from-env", SubscribeMessageRules.firstNonBlank("  ", "from-env"));
        assertEquals("from-env", SubscribeMessageRules.firstNonBlank(null, "from-env"));
        assertEquals("", SubscribeMessageRules.firstNonBlank(null, "  "));
        assertEquals("", SubscribeMessageRules.normalizeTemplateId(" placeholder-id "));
    }

    @Test
    void payAndShipPayloadsUseDocumentedKeywordNames() {
        Order order = paidOrder();
        order.setDvyFlowId("SF123456789CN");
        order.setDvyTime(new Date(1_747_000_000_000L));
        Map<String, Map<String, String>> pay = SubscribeMessageRules.payData(order);
        assertEquals("ORD-1", pay.get("character_string1").get("value"));
        assertEquals("88.50", pay.get("amount2").get("value"));
        assertEquals("支付成功", pay.get("thing5").get("value"));

        Map<String, Map<String, String>> ship = SubscribeMessageRules.shipData(order, "顺丰速运");
        assertEquals("顺丰速运", ship.get("thing2").get("value"));
        assertEquals("SF123456789CN", ship.get("character_string3").get("value"));
        assertTrue(SubscribeMessageRules.orderDetailPage("ORD-1").contains("orderNum=ORD-1"));
    }

    @Test
    void thingTruncatesToTwenty() {
        String longName = "一二三四五六七八九十一二三四五六七八九十超出";
        assertEquals(SubscribeMessageRules.THING_MAX, SubscribeMessageRules.thing(longName).length());
        assertTrue(SubscribeMessageRules.thing(longName).endsWith("…"));
        assertEquals("商品", SubscribeMessageRules.thing("  "));
    }

    private static Order paidOrder() {
        Order order = new Order();
        order.setOrderNumber("ORD-1");
        order.setProdName("测试商品");
        order.setActualTotal(88.5d);
        order.setPayTime(new Date());
        return order;
    }
}
