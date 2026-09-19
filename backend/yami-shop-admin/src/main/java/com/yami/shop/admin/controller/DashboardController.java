package com.yami.shop.admin.controller;

import com.yami.shop.bean.dto.DashboardOverviewDto;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.security.admin.util.SecurityUtils;
import com.yami.shop.service.DashboardStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端简易数据看板：GMV / 下单 / 已付 / 退款。无第三方密钥。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/order/dashboard")
public class DashboardController {

    private final DashboardStatsService dashboardStatsService;

    @GetMapping
    @PreAuthorize("@pms.hasPermission('order:dashboard:info')")
    public ServerResponseEntity<DashboardOverviewDto> overview() {
        Long shopId = SecurityUtils.getSysUser().getShopId();
        return ServerResponseEntity.success(dashboardStatsService.overview(shopId));
    }
}
