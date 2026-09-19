package com.yami.shop.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yami.shop.bean.model.Coupon;
import org.apache.ibatis.annotations.Param;

/**
 * 优惠券模板
 */
public interface CouponMapper extends BaseMapper<Coupon> {

    /**
     * 领取时扣库存。stocks=-1 不限量只碰投放状态。
     */
    int deductStock(@Param("couponId") Long couponId);
}
