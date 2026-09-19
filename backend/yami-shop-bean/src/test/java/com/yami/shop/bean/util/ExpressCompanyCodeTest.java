package com.yami.shop.bean.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpressCompanyCodeTest {

    @Test
    void fromSeedQueryUrl() {
        String url = "http://www.kuaidi100.com/query?type=shunfeng&postid={dvyFlowId}&id=11";
        assertEquals("shunfeng", ExpressCompanyCode.fromQueryUrl(url));
        assertEquals("yuantong", ExpressCompanyCode.fromQueryUrl(
                "http://www.kuaidi100.com/query?type=yuantong&postid={dvyFlowId}&id=11"));
    }

    @Test
    void fromCompanyNameUsesSeedAliases() {
        assertEquals("shunfeng", ExpressCompanyCode.fromCompanyName("顺丰快递公司"));
        assertEquals("shentong", ExpressCompanyCode.fromCompanyName("申通"));
        assertEquals("zhongtong", ExpressCompanyCode.fromCompanyName("中通速递"));
        assertEquals("jd", ExpressCompanyCode.fromCompanyName("京东快递"));
    }

    @Test
    void resolvePrefersQueryUrl() {
        assertEquals("ems", ExpressCompanyCode.resolve(
                "http://www.kuaidi100.com/query?type=ems&postid={dvyFlowId}&id=11",
                "顺丰快递公司"));
        assertEquals("yunda", ExpressCompanyCode.resolve(null, "韵达快递"));
        assertEquals("", ExpressCompanyCode.resolve("", "未知公司"));
    }
}
