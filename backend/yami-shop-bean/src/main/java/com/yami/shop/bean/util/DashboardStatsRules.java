package com.yami.shop.bean.util;

import com.yami.shop.bean.dto.DashboardAggRow;
import com.yami.shop.bean.dto.DashboardDailyDto;
import com.yami.shop.bean.dto.DashboardRangeDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 简易数据看板口径：自然日按 Asia/Shanghai；GMV 看支付时间，下单数看创建时间。
 */
public final class DashboardStatsRules {

    public static final String ZONE_ID = "Asia/Shanghai";
    public static final ZoneId ZONE = ZoneId.of(ZONE_ID);
    public static final DateTimeFormatter DAY = DateTimeFormatter.ISO_LOCAL_DATE;
    public static final LocalDate ALL_START_DATE = LocalDate.of(2000, 1, 1);
    public static final String NOTE = "GMV = 窗口内 pay_time 且 is_payed=1 的 actual_total 之和（含 mock 支付，不按退款冲减）。"
            + "下单数按 create_time。待付款=当前 status=1。关闭=当前 status=6。"
            + "退款成功=return_money_sts=1（按 refund_time）；处理中=return_money_sts=0（按 apply_time）。"
            + "净额=GMV-退款成功金额。不扣处理中退款。用户删除订单仍计入。";

    private DashboardStatsRules() {
    }

    public enum RangeCode {
        TODAY("今日"),
        LAST_7D("近7日"),
        LAST_30D("近30日"),
        ALL("累计");

        private final String label;

        RangeCode(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    public static final class Window {
        private final RangeCode code;
        private final Instant startInclusive;
        private final Instant endExclusive;

        private Window(RangeCode code, Instant startInclusive, Instant endExclusive) {
            this.code = code;
            this.startInclusive = startInclusive;
            this.endExclusive = endExclusive;
        }

        public RangeCode code() {
            return code;
        }

        public Date startTime() {
            return Date.from(startInclusive);
        }

        public Date endTime() {
            return Date.from(endExclusive);
        }

        public LocalDate startDate() {
            return startInclusive.atZone(ZONE).toLocalDate();
        }

        public LocalDate endDateExclusive() {
            return endExclusive.atZone(ZONE).toLocalDate();
        }
    }

    public static List<Window> defaultWindows(Clock clock) {
        ZonedDateTime now = ZonedDateTime.now(clock.withZone(ZONE));
        LocalDate today = now.toLocalDate();
        Instant end = today.plusDays(1).atStartOfDay(ZONE).toInstant();
        return List.of(
                window(RangeCode.TODAY, today.atStartOfDay(ZONE).toInstant(), end),
                window(RangeCode.LAST_7D, today.minusDays(6).atStartOfDay(ZONE).toInstant(), end),
                window(RangeCode.LAST_30D, today.minusDays(29).atStartOfDay(ZONE).toInstant(), end),
                window(RangeCode.ALL, ALL_START_DATE.atStartOfDay(ZONE).toInstant(), end)
        );
    }

    public static Window window(RangeCode code, Instant startInclusive, Instant endExclusive) {
        return new Window(code, startInclusive, endExclusive);
    }

    public static Window require(List<Window> windows, RangeCode code) {
        for (Window window : windows) {
            if (window.code() == code) {
                return window;
            }
        }
        throw new IllegalArgumentException("缺少统计窗口 " + code);
    }

    public static double money(Number value) {
        if (value == null) {
            return 0.0d;
        }
        return new BigDecimal(value.toString()).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public static long count(Number value) {
        return value == null ? 0L : value.longValue();
    }

    public static double netGmv(Number gmv, Number refundAmount) {
        return money(BigDecimal.valueOf(money(gmv)).subtract(BigDecimal.valueOf(money(refundAmount))));
    }

    public static DashboardRangeDto toRange(Window window, DashboardAggRow created, DashboardAggRow paid,
            DashboardAggRow refunded, DashboardAggRow pending) {
        DashboardRangeDto row = new DashboardRangeDto();
        row.setCode(window.code().name());
        row.setLabel(window.code().label());
        row.setStartTime(window.startTime());
        row.setEndTime(window.endTime());
        row.setOrderCount(count(created == null ? null : created.getOrderCount()));
        row.setUnpaidOrderCount(count(created == null ? null : created.getUnpaidOrderCount()));
        row.setClosedOrderCount(count(created == null ? null : created.getClosedOrderCount()));
        row.setUnpaidAmount(money(created == null ? null : created.getUnpaidAmount()));
        row.setPaidOrderCount(count(paid == null ? null : paid.getPaidOrderCount()));
        row.setGmv(money(paid == null ? null : paid.getGmv()));
        row.setRefundCount(count(refunded == null ? null : refunded.getRefundCount()));
        row.setRefundAmount(money(refunded == null ? null : refunded.getRefundAmount()));
        row.setPendingRefundCount(count(pending == null ? null : pending.getPendingRefundCount()));
        row.setPendingRefundAmount(money(pending == null ? null : pending.getPendingRefundAmount()));
        row.setNetGmv(netGmv(row.getGmv(), row.getRefundAmount()));
        return row;
    }

    public static List<DashboardDailyDto> mergeDaily(LocalDate startInclusive, LocalDate endExclusive,
            List<DashboardDailyDto> paidDays, List<DashboardDailyDto> refundDays) {
        Map<String, DashboardDailyDto> paid = index(paidDays);
        Map<String, DashboardDailyDto> refund = index(refundDays);
        List<DashboardDailyDto> out = new ArrayList<>();
        for (LocalDate day = startInclusive; day.isBefore(endExclusive); day = day.plusDays(1)) {
            String key = DAY.format(day);
            DashboardDailyDto p = paid.get(key);
            DashboardDailyDto r = refund.get(key);
            DashboardDailyDto row = new DashboardDailyDto();
            row.setStatDate(key);
            row.setPaidOrderCount(count(p == null ? null : p.getPaidOrderCount()));
            row.setGmv(money(p == null ? null : p.getGmv()));
            row.setRefundCount(count(r == null ? null : r.getRefundCount()));
            row.setRefundAmount(money(r == null ? null : r.getRefundAmount()));
            out.add(row);
        }
        return out;
    }

    private static Map<String, DashboardDailyDto> index(List<DashboardDailyDto> rows) {
        Map<String, DashboardDailyDto> map = new HashMap<>();
        if (rows == null) {
            return map;
        }
        for (DashboardDailyDto row : rows) {
            if (row != null && row.getStatDate() != null && !row.getStatDate().isBlank()) {
                map.put(row.getStatDate(), row);
            }
        }
        return map;
    }
}
