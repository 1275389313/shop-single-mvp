package com.yami.shop.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.shop.bean.app.dto.CouponDto;
import com.yami.shop.bean.app.dto.CouponOrderDto;
import com.yami.shop.bean.app.dto.ShopCartOrderDto;
import com.yami.shop.bean.app.param.OrderParam;
import com.yami.shop.bean.model.Coupon;
import com.yami.shop.bean.model.CouponUser;
import com.yami.shop.bean.util.CouponCalc;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.util.Arith;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.dao.CouponMapper;
import com.yami.shop.dao.CouponUserMapper;
import com.yami.shop.service.CouponService;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {

    private final CouponMapper couponMapper;
    private final CouponUserMapper couponUserMapper;

    @Override
    public void validateAndFill(Coupon coupon) {
        if (StrUtil.isBlank(coupon.getCouponName())) {
            throw new YamiShopBindException("请填写优惠券名称");
        }
        if (coupon.getCouponType() == null
                || (coupon.getCouponType() != CouponCalc.TYPE_REDUCE && coupon.getCouponType() != CouponCalc.TYPE_DISCOUNT)) {
            throw new YamiShopBindException("优惠券类型仅支持满减或折扣");
        }
        if (coupon.getCashCondition() == null || coupon.getCashCondition() < 0) {
            throw new YamiShopBindException("使用门槛不能为负数");
        }
        if (coupon.getCouponType() == CouponCalc.TYPE_REDUCE) {
            if (coupon.getReduceAmount() == null || coupon.getReduceAmount() <= 0) {
                throw new YamiShopBindException("请填写满减金额");
            }
        } else {
            Double discount = coupon.getCouponDiscount();
            if (discount == null || discount <= 0 || discount >= 10) {
                throw new YamiShopBindException("折扣请填写 0 到 10 之间的折数，如 8.5");
            }
        }
        if (coupon.getStartTime() == null || coupon.getEndTime() == null) {
            throw new YamiShopBindException("请填写领取起止时间");
        }
        if (!coupon.getStartTime().before(coupon.getEndTime())) {
            throw new YamiShopBindException("领取结束时间必须晚于开始时间");
        }
        if (coupon.getValidTimeType() == null) {
            coupon.setValidTimeType(1);
        }
        if (coupon.getValidTimeType() == 1) {
            if (coupon.getValidDays() == null || coupon.getValidDays() < 1) {
                throw new YamiShopBindException("领取后有效天数至少为 1");
            }
        } else if (coupon.getValidTimeType() == 2) {
            if (coupon.getValidStartTime() == null || coupon.getValidEndTime() == null) {
                throw new YamiShopBindException("请填写固定有效期");
            }
            if (!coupon.getValidStartTime().before(coupon.getValidEndTime())) {
                throw new YamiShopBindException("固定有效期结束必须晚于开始");
            }
        } else {
            throw new YamiShopBindException("有效期类型不正确");
        }
        if (coupon.getStocks() == null) {
            coupon.setStocks(0);
        }
        if (coupon.getSourceStock() == null) {
            coupon.setSourceStock(coupon.getStocks());
        }
        if (coupon.getLimitNum() == null) {
            coupon.setLimitNum(1);
        }
        if (coupon.getSuitableProdType() == null) {
            coupon.setSuitableProdType(0);
        }
        if (coupon.getStatus() == null) {
            coupon.setStatus(0);
        }
        if (coupon.getShopId() == null) {
            coupon.setShopId(1L);
        }
    }

    @Override
    public IPage<CouponDto> pageCenter(PageParam<CouponDto> page) {
        Date now = new Date();
        Page<Coupon> couponPage = couponMapper.selectPage(new Page<>(page.getCurrent(), page.getSize()),
                new LambdaQueryWrapper<Coupon>()
                        .eq(Coupon::getStatus, 1)
                        .le(Coupon::getStartTime, now)
                        .ge(Coupon::getEndTime, now)
                        .and(w -> w.eq(Coupon::getStocks, -1).or().gt(Coupon::getStocks, 0))
                        .orderByDesc(Coupon::getCouponId));
        Page<CouponDto> dtoPage = new Page<>(couponPage.getCurrent(), couponPage.getSize(), couponPage.getTotal());
        List<CouponDto> records = couponPage.getRecords().stream().map(c -> {
            CouponDto dto = toDto(c, null);
            dto.setCanReceive(true);
            return dto;
        }).collect(Collectors.toList());
        dtoPage.setRecords(records);
        return dtoPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receive(String userId, Long couponId) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null || !Objects.equals(coupon.getStatus(), 1)) {
            throw new YamiShopBindException("优惠券不存在或未投放");
        }
        Date now = new Date();
        if (now.before(coupon.getStartTime()) || now.after(coupon.getEndTime())) {
            throw new YamiShopBindException("不在领取时间内");
        }
        int limit = coupon.getLimitNum() == null ? 1 : coupon.getLimitNum();
        if (limit != -1) {
            int claimed = couponUserMapper.countByUserAndCoupon(userId, couponId);
            if (claimed >= limit) {
                throw new YamiShopBindException("已达领取上限");
            }
        }
        if (couponMapper.deductStock(couponId) == 0) {
            throw new YamiShopBindException("优惠券已领完");
        }
        CouponUser couponUser = new CouponUser();
        couponUser.setCouponId(couponId);
        couponUser.setUserId(userId);
        couponUser.setStatus(CouponUser.STS_UNUSED);
        couponUser.setReceiveTime(now);
        fillValidWindow(coupon, couponUser, now);
        couponUserMapper.insert(couponUser);
    }

    @Override
    public IPage<CouponDto> pageMine(String userId, Integer status, PageParam<CouponDto> page) {
        Date now = new Date();
        expireUnused(userId, now);
        LambdaQueryWrapper<CouponUser> wrapper = new LambdaQueryWrapper<CouponUser>()
                .eq(CouponUser::getUserId, userId)
                .orderByDesc(CouponUser::getCouponUserId);
        if (status != null) {
            wrapper.eq(CouponUser::getStatus, status);
        }
        Page<CouponUser> userPage = couponUserMapper.selectPage(new Page<>(page.getCurrent(), page.getSize()), wrapper);
        Page<CouponDto> dtoPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<CouponDto> records = new ArrayList<>();
        for (CouponUser cu : userPage.getRecords()) {
            Coupon coupon = couponMapper.selectById(cu.getCouponId());
            records.add(toDto(coupon, cu));
        }
        dtoPage.setRecords(records);
        return dtoPage;
    }

    @Override
    public void applyToConfirm(ShopCartOrderDto shopCartOrderDto, OrderParam orderParam, String userId) {
        double goodsTotal = shopCartOrderDto.getTotal() == null ? 0 : shopCartOrderDto.getTotal();
        Date now = new Date();
        expireUnused(userId, now);

        List<CouponUser> unused = couponUserMapper.selectList(new LambdaQueryWrapper<CouponUser>()
                .eq(CouponUser::getUserId, userId)
                .eq(CouponUser::getStatus, CouponUser.STS_UNUSED)
                .le(CouponUser::getUserStartTime, now)
                .ge(CouponUser::getUserEndTime, now)
                .orderByDesc(CouponUser::getCouponUserId));

        List<Long> selectedIds = orderParam.getCouponIds() == null
                ? Collections.emptyList()
                : orderParam.getCouponIds().stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        boolean userChanged = Objects.equals(orderParam.getUserChangeCoupon(), 1);

        if (selectedIds.size() > 1) {
            throw new YamiShopBindException("优惠券不能共用");
        }

        List<CouponOrderDto> coupons = new ArrayList<>();
        CouponOrderDto chosen = null;
        for (CouponUser cu : unused) {
            Coupon coupon = couponMapper.selectById(cu.getCouponId());
            CouponOrderDto dto = toOrderDto(cu, coupon, goodsTotal);
            boolean pick = selectedIds.contains(cu.getCouponUserId());
            dto.setChoose(pick);
            if (pick) {
                if (!Boolean.TRUE.equals(dto.getCanUse())) {
                    throw new YamiShopBindException("所选优惠券不满足使用条件");
                }
                chosen = dto;
            }
            coupons.add(dto);
        }
        if (userChanged && CollectionUtil.isNotEmpty(selectedIds) && chosen == null) {
            throw new YamiShopBindException("所选优惠券不可用");
        }
        if (!userChanged && chosen == null) {
            chosen = coupons.stream()
                    .filter(c -> Boolean.TRUE.equals(c.getCanUse()))
                    .max((a, b) -> Double.compare(
                            a.getCouponReduce() == null ? 0 : a.getCouponReduce(),
                            b.getCouponReduce() == null ? 0 : b.getCouponReduce()))
                    .orElse(null);
            if (chosen != null) {
                chosen.setChoose(true);
            }
        }

        double couponReduce = 0;
        if (chosen != null && Boolean.TRUE.equals(chosen.getChoose())) {
            couponReduce = chosen.getCouponReduce() == null ? 0 : chosen.getCouponReduce();
        }
        shopCartOrderDto.setCoupons(coupons);
        shopCartOrderDto.setCouponReduce(couponReduce);
        shopCartOrderDto.setShopReduce(Arith.add(
                shopCartOrderDto.getDiscountReduce() == null ? 0 : shopCartOrderDto.getDiscountReduce(),
                couponReduce));
        double transfee = shopCartOrderDto.getTransfee() == null ? 0 : shopCartOrderDto.getTransfee();
        shopCartOrderDto.setActualTotal(Arith.add(Arith.sub(goodsTotal, couponReduce), transfee));
        if (shopCartOrderDto.getActualTotal() < 0) {
            shopCartOrderDto.setActualTotal(0.0);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void useOnSubmit(ShopCartOrderDto shopCartOrderDto, String userId) {
        if (shopCartOrderDto == null || CollectionUtil.isEmpty(shopCartOrderDto.getCoupons())) {
            return;
        }
        String orderNumber = shopCartOrderDto.getOrderNumber();
        Date now = new Date();
        for (CouponOrderDto dto : shopCartOrderDto.getCoupons()) {
            if (!Boolean.TRUE.equals(dto.getChoose()) || dto.getCouponId() == null) {
                continue;
            }
            if (couponUserMapper.useCoupon(dto.getCouponId(), userId, orderNumber, now) == 0) {
                throw new YamiShopBindException("优惠券核销失败，请重新选择");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreByOrderNumber(String orderNumber) {
        if (StrUtil.isBlank(orderNumber)) {
            return;
        }
        couponUserMapper.restoreByOrderNumber(orderNumber, new Date());
    }

    private void expireUnused(String userId, Date now) {
        couponUserMapper.update(null, new LambdaUpdateWrapper<CouponUser>()
                .set(CouponUser::getStatus, CouponUser.STS_EXPIRED)
                .eq(CouponUser::getUserId, userId)
                .eq(CouponUser::getStatus, CouponUser.STS_UNUSED)
                .lt(CouponUser::getUserEndTime, now));
    }

    private void fillValidWindow(Coupon coupon, CouponUser couponUser, Date now) {
        if (Objects.equals(coupon.getValidTimeType(), 2)
                && coupon.getValidStartTime() != null
                && coupon.getValidEndTime() != null) {
            couponUser.setUserStartTime(coupon.getValidStartTime());
            couponUser.setUserEndTime(coupon.getValidEndTime());
            return;
        }
        int days = coupon.getValidDays() == null ? 30 : coupon.getValidDays();
        couponUser.setUserStartTime(now);
        couponUser.setUserEndTime(DateUtil.endOfDay(DateUtil.offsetDay(now, days)));
    }

    private CouponDto toDto(Coupon coupon, CouponUser cu) {
        CouponDto dto = new CouponDto();
        if (coupon != null) {
            BeanUtils.copyProperties(coupon, dto);
        }
        if (cu != null) {
            dto.setCouponUserId(cu.getCouponUserId());
            dto.setStatus(cu.getStatus());
            dto.setUserStartTime(cu.getUserStartTime());
            dto.setUserEndTime(cu.getUserEndTime());
        }
        return dto;
    }

    private CouponOrderDto toOrderDto(CouponUser cu, Coupon coupon, double goodsTotal) {
        CouponOrderDto dto = new CouponOrderDto();
        dto.setCouponId(cu.getCouponUserId());
        dto.setCouponTemplateId(cu.getCouponId());
        dto.setUserStartTime(cu.getUserStartTime());
        dto.setUserEndTime(cu.getUserEndTime());
        dto.setChoose(false);
        if (coupon == null) {
            dto.setCanUse(false);
            dto.setCouponReduce(0.0);
            return dto;
        }
        dto.setCouponName(coupon.getCouponName());
        dto.setSubTitle(coupon.getSubTitle());
        dto.setCouponType(coupon.getCouponType());
        dto.setCashCondition(coupon.getCashCondition());
        dto.setReduceAmount(coupon.getReduceAmount());
        dto.setCouponDiscount(coupon.getCouponDiscount());
        double reduce = CouponCalc.reduce(coupon, goodsTotal);
        dto.setCouponReduce(reduce);
        dto.setCanUse(reduce > 0);
        return dto;
    }
}
