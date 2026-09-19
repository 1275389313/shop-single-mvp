package com.yami.shop.admin.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.bean.dto.StockAlertConfigDto;
import com.yami.shop.bean.dto.StockAlertSkuDto;
import com.yami.shop.bean.model.Product;
import com.yami.shop.bean.model.Sku;
import com.yami.shop.bean.param.StockAlertConfigParam;
import com.yami.shop.bean.param.StockAlertSkuParam;
import com.yami.shop.bean.util.StockAlertRules;
import com.yami.shop.common.annotation.SysLog;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.security.admin.util.SecurityUtils;
import com.yami.shop.service.ProductService;
import com.yami.shop.service.SkuService;
import com.yami.shop.service.StockAlertService;
import com.yami.shop.sys.model.SysConfig;
import com.yami.shop.sys.service.SysConfigService;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.Objects;

/**
 * 管理端库存预警：全局阈值走 tz_sys_config，SKU 阈值走 tz_sku.stocks_arm，比较 tz_sku.stocks。
 */
@RestController
@AllArgsConstructor
@RequestMapping("/prod/stockAlert")
public class StockAlertController {

    private final SysConfigService sysConfigService;
    private final StockAlertService stockAlertService;
    private final SkuService skuService;
    private final ProductService productService;

    @GetMapping("/config")
    @PreAuthorize("@pms.hasPermission('prod:stockAlert:page')")
    public ServerResponseEntity<StockAlertConfigDto> config() {
        Long shopId = SecurityUtils.getSysUser().getShopId();
        int global = currentGlobal();
        StockAlertConfigDto dto = new StockAlertConfigDto();
        dto.setGlobalThreshold(global);
        dto.setCount(stockAlertService.countLowStock(shopId, global, 1));
        return ServerResponseEntity.success(dto);
    }

    @SysLog("修改库存预警全局阈值")
    @PutMapping("/config")
    @PreAuthorize("@pms.hasPermission('prod:stockAlert:update')")
    public ServerResponseEntity<Void> updateConfig(@Valid @RequestBody StockAlertConfigParam param) {
        int global;
        try {
            global = StockAlertRules.requireGlobal(param.getGlobalThreshold());
        } catch (IllegalArgumentException ex) {
            throw new YamiShopBindException(ex.getMessage());
        }
        String existing = sysConfigService.getValue(StockAlertRules.CONFIG_KEY);
        if (StrUtil.isBlank(existing)) {
            SysConfig config = new SysConfig();
            config.setParamKey(StockAlertRules.CONFIG_KEY);
            config.setParamValue(String.valueOf(global));
            config.setRemark("全局库存预警阈值。SKU 可售库存 stocks <= 该值时预警；SKU.stocks_arm 非空则覆盖（-1 关闭）");
            sysConfigService.save(config);
        } else {
            sysConfigService.updateValueByKey(StockAlertRules.CONFIG_KEY, String.valueOf(global));
        }
        return ServerResponseEntity.success();
    }

    @GetMapping("/page")
    @PreAuthorize("@pms.hasPermission('prod:stockAlert:page')")
    public ServerResponseEntity<IPage<StockAlertSkuDto>> page(PageParam<StockAlertSkuDto> page,
            @RequestParam(required = false) String prodName,
            @RequestParam(required = false) Integer prodStatus) {
        Long shopId = SecurityUtils.getSysUser().getShopId();
        int global = currentGlobal();
        String name = StrUtil.isBlank(prodName) ? null : prodName.trim();
        return ServerResponseEntity.success(
                stockAlertService.pageLowStock(page, shopId, global, name, prodStatus));
    }

    @SysLog("修改SKU库存预警阈值")
    @PutMapping("/sku")
    @PreAuthorize("@pms.hasPermission('prod:stockAlert:update')")
    public ServerResponseEntity<Void> updateSku(@Valid @RequestBody StockAlertSkuParam param) {
        Integer stocksArm;
        try {
            stocksArm = StockAlertRules.normalizeSkuThreshold(param.getStocksArm());
        } catch (IllegalArgumentException ex) {
            throw new YamiShopBindException(ex.getMessage());
        }
        Sku sku = skuService.getById(param.getSkuId());
        if (sku == null || Objects.equals(sku.getIsDelete(), 1)) {
            throw new YamiShopBindException("SKU 不存在");
        }
        Product product = productService.getById(sku.getProdId());
        if (product == null || !Objects.equals(product.getShopId(), SecurityUtils.getSysUser().getShopId())) {
            throw new YamiShopBindException("没有权限修改该 SKU");
        }
        skuService.update(new LambdaUpdateWrapper<Sku>()
                .eq(Sku::getSkuId, sku.getSkuId())
                .set(Sku::getStocksArm, stocksArm));
        skuService.removeSkuCacheBySkuId(sku.getSkuId(), sku.getProdId());
        return ServerResponseEntity.success();
    }

    private int currentGlobal() {
        return StockAlertRules.parseGlobal(sysConfigService.getValue(StockAlertRules.CONFIG_KEY));
    }
}
