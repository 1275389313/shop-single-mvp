package com.yami.shop.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.bean.model.Coupon;
import com.yami.shop.bean.model.CouponUser;
import com.yami.shop.common.annotation.SysLog;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.dao.CouponUserMapper;
import com.yami.shop.security.admin.util.SecurityUtils;
import com.yami.shop.service.CouponService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.Date;
import java.util.Objects;

/**
 * 管理端优惠券。
 */
@RestController
@AllArgsConstructor
@RequestMapping("/coupon/coupon")
public class CouponController {

    private final CouponService couponService;
    private final CouponUserMapper couponUserMapper;

    @GetMapping("/page")
    @PreAuthorize("@pms.hasPermission('coupon:coupon:page')")
    public ServerResponseEntity<IPage<Coupon>> page(Coupon coupon, PageParam<Coupon> page) {
        Long shopId = SecurityUtils.getSysUser().getShopId();
        IPage<Coupon> couponPage = couponService.page(page, new LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getShopId, shopId)
                .like(coupon.getCouponName() != null, Coupon::getCouponName, coupon.getCouponName())
                .eq(coupon.getCouponType() != null, Coupon::getCouponType, coupon.getCouponType())
                .eq(coupon.getStatus() != null, Coupon::getStatus, coupon.getStatus())
                .orderByDesc(Coupon::getCouponId));
        return ServerResponseEntity.success(couponPage);
    }

    @GetMapping("/info/{id}")
    @PreAuthorize("@pms.hasPermission('coupon:coupon:info')")
    public ServerResponseEntity<Coupon> info(@PathVariable("id") Long id) {
        return ServerResponseEntity.success(owned(id));
    }

    @SysLog("新增优惠券")
    @PostMapping
    @PreAuthorize("@pms.hasPermission('coupon:coupon:save')")
    public ServerResponseEntity<Void> save(@RequestBody @Valid Coupon coupon) {
        coupon.setCouponId(null);
        coupon.setShopId(SecurityUtils.getSysUser().getShopId());
        couponService.validateAndFill(coupon);
        coupon.setCreateTime(new Date());
        coupon.setUpdateTime(new Date());
        if (coupon.getSourceStock() == null) {
            coupon.setSourceStock(coupon.getStocks());
        }
        couponService.save(coupon);
        return ServerResponseEntity.success();
    }

    @SysLog("修改优惠券")
    @PutMapping
    @PreAuthorize("@pms.hasPermission('coupon:coupon:update')")
    public ServerResponseEntity<Void> update(@RequestBody @Valid Coupon coupon) {
        Coupon db = owned(coupon.getCouponId());
        coupon.setShopId(db.getShopId());
        couponService.validateAndFill(coupon);
        coupon.setUpdateTime(new Date());
        couponService.updateById(coupon);
        return ServerResponseEntity.success();
    }

    @SysLog("删除优惠券")
    @DeleteMapping("/{id}")
    @PreAuthorize("@pms.hasPermission('coupon:coupon:delete')")
    public ServerResponseEntity<Void> delete(@PathVariable Long id) {
        owned(id);
        Long claimed = couponUserMapper.selectCount(new LambdaQueryWrapper<CouponUser>()
                .eq(CouponUser::getCouponId, id));
        if (claimed != null && claimed > 0) {
            throw new YamiShopBindException("已有用户领取，不能删除，请下线");
        }
        couponService.removeById(id);
        return ServerResponseEntity.success();
    }

    private Coupon owned(Long id) {
        Coupon coupon = couponService.getById(id);
        if (coupon == null || !Objects.equals(coupon.getShopId(), SecurityUtils.getSysUser().getShopId())) {
            throw new YamiShopBindException("优惠券不存在");
        }
        return coupon;
    }
}
