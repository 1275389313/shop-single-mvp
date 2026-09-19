package com.yami.shop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.shop.bean.dto.StockAlertSkuDto;

/**
 * 库存预警查询（SKU 可售库存）。
 */
public interface StockAlertService {

    IPage<StockAlertSkuDto> pageLowStock(Page<StockAlertSkuDto> page, Long shopId, int globalThreshold,
            String prodName, Integer prodStatus);

    long countLowStock(Long shopId, int globalThreshold, Integer prodStatus);
}
