package com.yami.shop.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yami.shop.bean.model.CouponUser;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

/**
 * 用户优惠券
 */
public interface CouponUserMapper extends BaseMapper<CouponUser> {

    int countByUserAndCoupon(@Param("userId") String userId, @Param("couponId") Long couponId);

    /**
     * 核销：未使用且在有效期内。
     */
    int useCoupon(@Param("couponUserId") Long couponUserId,
                  @Param("userId") String userId,
                  @Param("orderNumber") String orderNumber,
                  @Param("now") Date now);

    /**
     * 未支付取消：已核销改回未使用。
     */
    int restoreByOrderNumber(@Param("orderNumber") String orderNumber, @Param("now") Date now);
}
