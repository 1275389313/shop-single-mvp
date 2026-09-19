package com.yami.shop.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.shop.bean.dto.StockAlertSkuDto;
import com.yami.shop.dao.SkuMapper;
import com.yami.shop.service.StockAlertService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author lanhai
 */
@Service
@AllArgsConstructor
public class StockAlertServiceImpl implements StockAlertService {

    private final SkuMapper skuMapper;

    @Override
    public IPage<StockAlertSkuDto> pageLowStock(Page<StockAlertSkuDto> page, Long shopId, int globalThreshold,
            String prodName, Integer prodStatus) {
        return skuMapper.pageLowStock(page, shopId, globalThreshold, prodName, prodStatus);
    }

    @Override
    public long countLowStock(Long shopId, int globalThreshold, Integer prodStatus) {
        Long count = skuMapper.countLowStock(shopId, globalThreshold, null, prodStatus);
        return count == null ? 0L : count;
    }
}
