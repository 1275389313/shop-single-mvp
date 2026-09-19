package com.yami.shop.dao;

import com.yami.shop.bean.dto.DashboardAggRow;
import com.yami.shop.bean.dto.DashboardDailyDto;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 管理端数据看板聚合。开源 mall4j 无统计报表接口，直接扫 tz_order / tz_order_refund。
 */
public interface DashboardStatsMapper {

    DashboardAggRow aggregateCreated(@Param("shopId") Long shopId, @Param("startTime") Date startTime,
            @Param("endTime") Date endTime);

    DashboardAggRow aggregatePaid(@Param("shopId") Long shopId, @Param("startTime") Date startTime,
            @Param("endTime") Date endTime);

    DashboardAggRow aggregateRefunded(@Param("shopId") Long shopId, @Param("startTime") Date startTime,
            @Param("endTime") Date endTime);

    DashboardAggRow aggregatePendingRefund(@Param("shopId") Long shopId, @Param("startTime") Date startTime,
            @Param("endTime") Date endTime);

    List<DashboardDailyDto> listPaidDaily(@Param("shopId") Long shopId, @Param("startTime") Date startTime,
            @Param("endTime") Date endTime);

    List<DashboardDailyDto> listRefundDaily(@Param("shopId") Long shopId, @Param("startTime") Date startTime,
            @Param("endTime") Date endTime);
}
