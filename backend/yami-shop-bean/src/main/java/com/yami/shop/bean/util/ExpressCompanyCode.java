package com.yami.shop.bean.util;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Maps mall4j {@code tz_delivery.query_url} / company names to 快递100 {@code com} codes.
 * The seed SQL stores {@code http://www.kuaidi100.com/query?type=shunfeng&postid={dvyFlowId}&id=11}.
 */
public final class ExpressCompanyCode {

    private static final Map<String, String> NAME_TO_CODE = new LinkedHashMap<>();

    static {
        NAME_TO_CODE.put("顺丰", "shunfeng");
        NAME_TO_CODE.put("申通", "shentong");
        NAME_TO_CODE.put("中通", "zhongtong");
        NAME_TO_CODE.put("圆通", "yuantong");
        NAME_TO_CODE.put("韵达", "yunda");
        NAME_TO_CODE.put("ems", "ems");
        NAME_TO_CODE.put("邮政", "youzhengguonei");
        NAME_TO_CODE.put("汇通", "huitongkuaidi");
        NAME_TO_CODE.put("百世", "huitongkuaidi");
        NAME_TO_CODE.put("天天", "tiantian");
        NAME_TO_CODE.put("宅急送", "zhaijisong");
        NAME_TO_CODE.put("优速", "youshuwuliu");
        NAME_TO_CODE.put("中邮", "zhongyouwuliu");
        NAME_TO_CODE.put("信丰", "xinfengwuliu");
        NAME_TO_CODE.put("速尔", "suer");
        NAME_TO_CODE.put("佳吉", "jiajiwuliu");
        NAME_TO_CODE.put("安信达", "anxindakuaixi");
        NAME_TO_CODE.put("如风达", "rufengda");
        NAME_TO_CODE.put("凡客", "rufengda");
        NAME_TO_CODE.put("京东", "jd");
        NAME_TO_CODE.put("德邦", "debangwuliu");
        NAME_TO_CODE.put("极兔", "jtexpress");
        NAME_TO_CODE.put("丹鸟", "danniao");
    }

    private ExpressCompanyCode() {
    }

    /**
     * Reads {@code type=} from the mall4j query URL template.
     */
    public static String fromQueryUrl(String queryUrl) {
        if (queryUrl == null || queryUrl.isBlank()) {
            return "";
        }
        int typeIdx = queryUrl.toLowerCase(Locale.ROOT).indexOf("type=");
        if (typeIdx < 0) {
            return "";
        }
        int start = typeIdx + 5;
        int end = queryUrl.indexOf('&', start);
        String raw = end < 0 ? queryUrl.substring(start) : queryUrl.substring(start, end);
        return raw.trim().toLowerCase(Locale.ROOT);
    }

    public static String fromCompanyName(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }
        String compact = name.toLowerCase(Locale.ROOT).replaceAll("[\\s·\\.]", "");
        for (Map.Entry<String, String> entry : NAME_TO_CODE.entrySet()) {
            if (compact.contains(entry.getKey().toLowerCase(Locale.ROOT))) {
                return entry.getValue();
            }
        }
        return "";
    }

    public static String resolve(String queryUrl, String companyName) {
        String fromUrl = fromQueryUrl(queryUrl);
        if (!fromUrl.isBlank()) {
            return fromUrl;
        }
        return fromCompanyName(companyName);
    }
}
