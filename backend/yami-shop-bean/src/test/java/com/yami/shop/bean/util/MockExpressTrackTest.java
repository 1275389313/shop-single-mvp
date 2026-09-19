package com.yami.shop.bean.util;

import com.yami.shop.bean.app.dto.DeliveryDto;
import com.yami.shop.bean.app.dto.DeliveryInfoDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MockExpressTrackTest {

    private static final ZoneId CN = ZoneId.of("Asia/Shanghai");

    @Test
    void shipmentInTransitOmitsFutureAndSign() {
        Date ship = date(2026, 9, 18, 8, 0);
        Date now = date(2026, 9, 19, 6, 45);
        DeliveryDto dto = MockExpressTrack.shipment("圆通快递", "YT1234567890", ship, now, false, "广州市", "杭州市");
        assertTrue(Boolean.TRUE.equals(dto.getMock()));
        assertEquals("mock", dto.getSource());
        assertEquals("SHIPMENT", dto.getDirection());
        assertEquals("YT1234567890", dto.getDvyFlowId());
        assertEquals("圆通快递", dto.getCompanyName());
        assertFalse(dto.getData().isEmpty());
        assertTrue(dto.getData().get(0).getContext().contains("杭州市"));
        assertFalse(dto.getData().stream().anyMatch(item -> item.getContext().contains("已签收")));
        assertTimesNewestFirst(dto.getData());
    }

    @Test
    void shipmentSignedIncludesSign() {
        Date ship = date(2026, 9, 17, 8, 0);
        Date now = date(2026, 9, 19, 6, 45);
        DeliveryDto dto = MockExpressTrack.shipment("顺丰快递公司", "SF111", ship, now, true, "广州市", "杭州市");
        assertEquals("3", dto.getState());
        assertEquals("已签收", dto.getStateText());
        assertTrue(dto.getData().get(0).getContext().contains("已签收"));
        assertTrue(dto.getData().get(dto.getData().size() - 1).getContext().contains("已揽收"));
    }

    @Test
    void returnSignedUsesMerchantCopy() {
        Date ship = date(2026, 9, 17, 10, 0);
        Date now = date(2026, 9, 19, 6, 45);
        DeliveryDto dto = MockExpressTrack.returned("申通快递公司", "STO999", ship, now, true, "杭州市", "广州市");
        assertEquals("RETURN", dto.getDirection());
        assertTrue(dto.getData().get(0).getContext().contains("商家已签收"));
        assertTrue(dto.getMessage().contains("模拟轨迹"));
    }

    @Test
    void emptyKeepsCompanyAndMessage() {
        DeliveryDto dto = MockExpressTrack.empty("中通速递", "", MockExpressTrack.DIRECTION_SHIPMENT, "订单尚未发货");
        assertTrue(dto.getData().isEmpty());
        assertEquals("中通速递", dto.getCompanyName());
        assertEquals("订单尚未发货", dto.getMessage());
    }

    private static void assertTimesNewestFirst(List<DeliveryInfoDto> data) {
        for (int i = 1; i < data.size(); i++) {
            assertTrue(data.get(i - 1).getTime().compareTo(data.get(i).getTime()) >= 0);
        }
    }

    private static Date date(int y, int m, int d, int h, int min) {
        return Date.from(LocalDateTime.of(y, m, d, h, min).atZone(CN).toInstant());
    }
}
