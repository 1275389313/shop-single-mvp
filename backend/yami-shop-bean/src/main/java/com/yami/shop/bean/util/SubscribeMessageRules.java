package com.yami.shop.bean.util;

import com.yami.shop.bean.model.Order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * WeChat mini-program subscribe-message skip/send rules.
 * Template IDs are not secrets. AppSecret stays in env and is never required to run.
 */
public final class SubscribeMessageRules {

    public static final String PAY_TEMPLATE_KEY = "WX_SUBSCRIBE_PAY_TEMPLATE_ID";
    public static final String SHIP_TEMPLATE_KEY = "WX_SUBSCRIBE_SHIP_TEMPLATE_ID";
    public static final String ORDER_DETAIL_PAGE = "pages/order-detail/order-detail";
    public static final int THING_MAX = 20;
    public static final int CHARACTER_MAX = 32;

    private SubscribeMessageRules() {
    }

    public enum Scene {
        PAY_SUCCESS,
        SHIPMENT
    }

    public enum Decision {
        SEND,
        SKIP_MISSING_TEMPLATE,
        SKIP_MISSING_CREDENTIALS,
        SKIP_MISSING_OPENID,
        SKIP_MOCK_OPENID,
        SKIP_NO_ORDER
    }

    public static String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    /**
     * Empty, "-", "none", "unset", "todo", YOUR_* placeholders are treated as unset.
     */
    public static String normalizeTemplateId(String raw) {
        String value = firstNonBlank(raw);
        if (value.isEmpty()) {
            return "";
        }
        String lower = value.toLowerCase(Locale.ROOT);
        if ("-".equals(value) || "none".equals(lower) || "unset".equals(lower) || "todo".equals(lower)
                || value.regionMatches(true, 0, "YOUR_", 0, 5) || lower.contains("placeholder")) {
            return "";
        }
        return value;
    }

    public static boolean isMockOpenId(String openId) {
        if (openId == null || openId.isBlank()) {
            return false;
        }
        String trimmed = openId.trim();
        return trimmed.regionMatches(true, 0, "mock_", 0, 5);
    }

    public static Decision decide(String templateId, String appId, String appSecret, String openId, Order order) {
        if (order == null || isBlank(order.getOrderNumber())) {
            return Decision.SKIP_NO_ORDER;
        }
        if (isBlank(normalizeTemplateId(templateId))) {
            return Decision.SKIP_MISSING_TEMPLATE;
        }
        if (isBlank(openId)) {
            return Decision.SKIP_MISSING_OPENID;
        }
        if (isMockOpenId(openId)) {
            return Decision.SKIP_MOCK_OPENID;
        }
        if (isBlank(appId) || isBlank(appSecret)) {
            return Decision.SKIP_MISSING_CREDENTIALS;
        }
        return Decision.SEND;
    }

    public static String skipLog(Decision decision, Scene scene, String orderNumber) {
        String sceneName = scene == null ? "unknown" : scene.name();
        String order = orderNumber == null ? "" : orderNumber;
        return switch (decision) {
            case SKIP_MISSING_TEMPLATE -> "skip subscribe " + sceneName + " order=" + order + " reason=templateId unset";
            case SKIP_MISSING_CREDENTIALS -> "skip subscribe " + sceneName + " order=" + order
                    + " reason=WX_APP_ID/WX_APP_SECRET unset";
            case SKIP_MISSING_OPENID -> "skip subscribe " + sceneName + " order=" + order + " reason=user has no wxOpenId";
            case SKIP_MOCK_OPENID -> "skip subscribe " + sceneName + " order=" + order
                    + " reason=mock openId (mock wechat login)";
            case SKIP_NO_ORDER -> "skip subscribe " + sceneName + " reason=order missing";
            case SEND -> "send subscribe " + sceneName + " order=" + order;
        };
    }

    public static String orderDetailPage(String orderNumber) {
        if (isBlank(orderNumber)) {
            return ORDER_DETAIL_PAGE;
        }
        return ORDER_DETAIL_PAGE + "?orderNum=" + orderNumber.trim();
    }

    public static String thing(String raw) {
        return clip(raw, THING_MAX, "商品");
    }

    public static String characterString(String raw) {
        return clip(raw, CHARACTER_MAX, "-");
    }

    public static String amount(Double value) {
        BigDecimal n = value == null ? BigDecimal.ZERO : BigDecimal.valueOf(value);
        return n.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    public static String time(Date date) {
        Date use = date == null ? new Date() : date;
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(use);
    }

    /**
     * Keyword names are a documented default. Merchants must match these in the MP template,
     * or change this map. WeChat errcode 47003 means a field mismatch — logged, never thrown.
     */
    public static Map<String, Map<String, String>> payData(Order order) {
        Map<String, Map<String, String>> data = new LinkedHashMap<>();
        data.put("character_string1", value(characterString(order.getOrderNumber())));
        data.put("amount2", value(amount(order.getActualTotal())));
        data.put("thing3", value(thing(order.getProdName())));
        data.put("time4", value(time(order.getPayTime())));
        data.put("thing5", value(thing("支付成功")));
        return data;
    }

    public static Map<String, Map<String, String>> shipData(Order order, String companyName) {
        Map<String, Map<String, String>> data = new LinkedHashMap<>();
        data.put("character_string1", value(characterString(order.getOrderNumber())));
        data.put("thing2", value(thing(firstNonBlank(companyName, "快递"))));
        data.put("character_string3", value(characterString(order.getDvyFlowId())));
        data.put("thing4", value(thing(order.getProdName())));
        data.put("time5", value(time(order.getDvyTime())));
        return data;
    }

    private static Map<String, String> value(String v) {
        Map<String, String> node = new LinkedHashMap<>();
        node.put("value", v);
        return node;
    }

    private static String clip(String raw, int max, String fallback) {
        String v = firstNonBlank(raw);
        if (v.isEmpty()) {
            v = fallback;
        }
        v = v.replace('\n', ' ').replace('\r', ' ').trim();
        if (v.length() <= max) {
            return v;
        }
        if (max <= 1) {
            return v.substring(0, max);
        }
        return v.substring(0, max - 1) + "…";
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
