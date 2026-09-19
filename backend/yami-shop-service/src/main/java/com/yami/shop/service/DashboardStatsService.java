package com.yami.shop.service;

import com.yami.shop.bean.dto.DashboardOverviewDto;

/**
 * 管理端简易 GMV / 订单看板。
 */
public interface DashboardStatsService {

    DashboardOverviewDto overview(Long shopId);
}
