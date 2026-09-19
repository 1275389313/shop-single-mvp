package com.yami.shop.bean.util;

import com.yami.shop.bean.model.Coupon;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CouponCalcTest {

    @Test
    void reduceCouponMeetsThreshold() {
        Coupon coupon = reduce(50, 10);
        assertEquals(10.0, CouponCalc.reduce(coupon, 50), 0.001);
        assertEquals(10.0, CouponCalc.reduce(coupon, 80), 0.001);
        assertTrue(CouponCalc.canUse(coupon, 50));
    }

    @Test
    void reduceCouponBelowThreshold() {
        Coupon coupon = reduce(50, 10);
        assertEquals(0.0, CouponCalc.reduce(coupon, 49.99), 0.001);
        assertFalse(CouponCalc.canUse(coupon, 49.99));
    }

    @Test
    void reduceCannotExceedGoodsTotal() {
        Coupon coupon = reduce(0, 30);
        assertEquals(20.0, CouponCalc.reduce(coupon, 20), 0.001);
    }

    @Test
    void discountCoupon() {
        Coupon coupon = discount(20, 8.5, null);
        // 100 * 15% = 15
        assertEquals(15.0, CouponCalc.reduce(coupon, 100), 0.001);
        assertEquals(0.0, CouponCalc.reduce(coupon, 19.99), 0.001);
    }

    @Test
    void discountCouponCapped() {
        Coupon coupon = discount(0, 5.0, 10.0);
        // 50% of 100 is 50, cap 10
        assertEquals(10.0, CouponCalc.reduce(coupon, 100), 0.001);
    }

    @Test
    void invalidDiscountIgnored() {
        Coupon coupon = discount(0, 10.0, null);
        assertEquals(0.0, CouponCalc.reduce(coupon, 100), 0.001);
        coupon.setCouponDiscount(0.0);
        assertEquals(0.0, CouponCalc.reduce(coupon, 100), 0.001);
    }

    private static Coupon reduce(double condition, double amount) {
        Coupon coupon = new Coupon();
        coupon.setCouponType(CouponCalc.TYPE_REDUCE);
        coupon.setCashCondition(condition);
        coupon.setReduceAmount(amount);
        return coupon;
    }

    private static Coupon discount(double condition, double discount, Double cap) {
        Coupon coupon = new Coupon();
        coupon.setCouponType(CouponCalc.TYPE_DISCOUNT);
        coupon.setCashCondition(condition);
        coupon.setCouponDiscount(discount);
        coupon.setMaxReduceAmount(cap);
        return coupon;
    }
}
