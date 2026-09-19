package com.yami.shop.service.impl;

import com.yami.shop.bean.dto.DashboardOverviewDto;
import com.yami.shop.bean.dto.DashboardRangeDto;
import com.yami.shop.bean.util.DashboardStatsRules;
import com.yami.shop.common.config.ShopMvpProperties;
import com.yami.shop.dao.DashboardStatsMapper;
import com.yami.shop.service.DashboardStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 按店铺聚合 tz_order / tz_order_refund。不改支付、不读密钥。
 */
@Service
@RequiredArgsConstructor
public class DashboardStatsServiceImpl implements DashboardStatsService {

    private final DashboardStatsMapper dashboardStatsMapper;
    private final ShopMvpProperties shopMvpProperties;

    @Override
    public DashboardOverviewDto overview(Long shopId) {
        Clock clock = Clock.system(DashboardStatsRules.ZONE);
        List<DashboardStatsRules.Window> windows = DashboardStatsRules.defaultWindows(clock);
        DashboardOverviewDto dto = new DashboardOverviewDto();
        dto.setTimezone(DashboardStatsRules.ZONE_ID);
        dto.setGeneratedAt(Date.from(Instant.now(clock)));
        dto.setMockPay(shopMvpProperties.getMock().isPay());
        dto.setNote(DashboardStatsRules.NOTE);

        List<DashboardRangeDto> ranges = new ArrayList<>();
        for (DashboardStatsRules.Window window : windows) {
            ranges.add(DashboardStatsRules.toRange(
                    window,
                    dashboardStatsMapper.aggregateCreated(shopId, window.startTime(), window.endTime()),
                    dashboardStatsMapper.aggregatePaid(shopId, window.startTime(), window.endTime()),
                    dashboardStatsMapper.aggregateRefunded(shopId, window.startTime(), window.endTime()),
                    dashboardStatsMapper.aggregatePendingRefund(shopId, window.startTime(), window.endTime())
            ));
        }
        dto.setRanges(ranges);

        DashboardStatsRules.Window last30 = DashboardStatsRules.require(windows, DashboardStatsRules.RangeCode.LAST_30D);
        dto.setDaily(DashboardStatsRules.mergeDaily(
                last30.startDate(),
                last30.endDateExclusive(),
                dashboardStatsMapper.listPaidDaily(shopId, last30.startTime(), last30.endTime()),
                dashboardStatsMapper.listRefundDaily(shopId, last30.startTime(), last30.endTime())
        ));
        return dto;
    }
}
