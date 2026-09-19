package com.yami.shop.bean.dto;

import lombok.Data;

/**
 * Mapper 聚合行。各查询只填用到的字段，其余为 null。
 */
@Data
public class DashboardAggRow {

    private Long orderCount;

    private Long unpaidOrderCount;

    private Long paidOrderCount;

    private Long closedOrderCount;

    private Double unpaidAmount;

    private Double gmv;

    private Long refundCount;

    private Double refundAmount;

    private Long pendingRefundCount;

    private Double pendingRefundAmount;
}
