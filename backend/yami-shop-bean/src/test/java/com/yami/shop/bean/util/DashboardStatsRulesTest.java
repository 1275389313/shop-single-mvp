package com.yami.shop.bean.util;

import com.yami.shop.bean.dto.DashboardAggRow;
import com.yami.shop.bean.dto.DashboardDailyDto;
import com.yami.shop.bean.dto.DashboardRangeDto;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DashboardStatsRulesTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-19T08:00:00Z"), DashboardStatsRules.ZONE);

    @Test
    void windowsAreShanghaiCalendarDaysIncludingToday() {
        List<DashboardStatsRules.Window> windows = DashboardStatsRules.defaultWindows(CLOCK);
        assertEquals(4, windows.size());

        DashboardStatsRules.Window today = DashboardStatsRules.require(windows, DashboardStatsRules.RangeCode.TODAY);
        assertEquals(LocalDate.of(2026, 9, 19), today.startDate());
        assertEquals(LocalDate.of(2026, 9, 20), today.endDateExclusive());

        DashboardStatsRules.Window week = DashboardStatsRules.require(windows, DashboardStatsRules.RangeCode.LAST_7D);
        assertEquals(LocalDate.of(2026, 9, 13), week.startDate());
        assertEquals(LocalDate.of(2026, 9, 20), week.endDateExclusive());

        DashboardStatsRules.Window month = DashboardStatsRules.require(windows, DashboardStatsRules.RangeCode.LAST_30D);
        assertEquals(LocalDate.of(2026, 8, 21), month.startDate());

        DashboardStatsRules.Window all = DashboardStatsRules.require(windows, DashboardStatsRules.RangeCode.ALL);
        assertEquals(DashboardStatsRules.ALL_START_DATE, all.startDate());
        assertTrue(all.startTime().before(today.startTime()));
    }

    @Test
    void utcMorningStillCountsAsShanghaiCalendarDate() {
        Clock utcMorning = Clock.fixed(Instant.parse("2026-09-18T16:30:00Z"), ZoneOffset.UTC);
        DashboardStatsRules.Window today = DashboardStatsRules.require(
                DashboardStatsRules.defaultWindows(utcMorning), DashboardStatsRules.RangeCode.TODAY);
        assertEquals(LocalDate.of(2026, 9, 19), today.startDate());
    }

    @Test
    void moneyAndNetRoundToCents() {
        assertEquals(0.0d, DashboardStatsRules.money(null));
        assertEquals(1.24d, DashboardStatsRules.money(1.235d));
        assertEquals(10.0d, DashboardStatsRules.netGmv(12.345, 2.345));
        assertEquals(0L, DashboardStatsRules.count(null));
        assertEquals(3L, DashboardStatsRules.count(3));
    }

    @Test
    void toRangeMergesCreatedPaidAndRefunds() {
        DashboardStatsRules.Window today = DashboardStatsRules.require(
                DashboardStatsRules.defaultWindows(CLOCK), DashboardStatsRules.RangeCode.TODAY);
        DashboardAggRow created = new DashboardAggRow();
        created.setOrderCount(5L);
        created.setUnpaidOrderCount(2L);
        created.setClosedOrderCount(1L);
        created.setUnpaidAmount(30.1);
        DashboardAggRow paid = new DashboardAggRow();
        paid.setPaidOrderCount(3L);
        paid.setGmv(100.0);
        DashboardAggRow refunded = new DashboardAggRow();
        refunded.setRefundCount(1L);
        refunded.setRefundAmount(20.0);
        DashboardAggRow pending = new DashboardAggRow();
        pending.setPendingRefundCount(1L);
        pending.setPendingRefundAmount(5.5);

        DashboardRangeDto row = DashboardStatsRules.toRange(today, created, paid, refunded, pending);
        assertEquals("TODAY", row.getCode());
        assertEquals("今日", row.getLabel());
        assertEquals(5L, row.getOrderCount());
        assertEquals(2L, row.getUnpaidOrderCount());
        assertEquals(3L, row.getPaidOrderCount());
        assertEquals(100.0d, row.getGmv());
        assertEquals(20.0d, row.getRefundAmount());
        assertEquals(80.0d, row.getNetGmv());
        assertEquals(5.5d, row.getPendingRefundAmount());
    }

    @Test
    void mergeDailyFillsMissingDays() {
        DashboardDailyDto paid = new DashboardDailyDto();
        paid.setStatDate("2026-09-19");
        paid.setPaidOrderCount(2L);
        paid.setGmv(9.9);
        DashboardDailyDto refund = new DashboardDailyDto();
        refund.setStatDate("2026-09-18");
        refund.setRefundCount(1L);
        refund.setRefundAmount(1.1);

        List<DashboardDailyDto> days = DashboardStatsRules.mergeDaily(
                LocalDate.of(2026, 9, 17),
                LocalDate.of(2026, 9, 20),
                List.of(paid),
                List.of(refund));
        assertEquals(3, days.size());
        assertEquals("2026-09-17", days.get(0).getStatDate());
        assertEquals(0.0d, days.get(0).getGmv());
        assertEquals(0L, days.get(0).getPaidOrderCount());
        assertEquals(1.1d, days.get(1).getRefundAmount());
        assertEquals(9.9d, days.get(2).getGmv());
        assertEquals(2L, days.get(2).getPaidOrderCount());
        assertEquals(0.0d, days.get(2).getRefundAmount());
    }
}
