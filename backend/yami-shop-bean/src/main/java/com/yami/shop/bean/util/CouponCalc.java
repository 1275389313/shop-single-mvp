package com.yami.shop.bean.util;

import com.yami.shop.bean.model.Coupon;
import com.yami.shop.common.util.Arith;

/**
 * 优惠券金额规则：满减 / 折扣。只按商品总额计算，不含运费。
 */
public final class CouponCalc {

    public static final int TYPE_REDUCE = 1;
    public static final int TYPE_DISCOUNT = 2;

    private CouponCalc() {
    }

    /**
     * @return 优惠金额，达不到门槛或不合法时为 0
     */
    public static double reduce(Coupon coupon, double goodsTotal) {
        if (coupon == null || goodsTotal <= 0) {
            return 0;
        }
        double condition = coupon.getCashCondition() == null ? 0 : coupon.getCashCondition();
        if (goodsTotal + 1e-9 < condition) {
            return 0;
        }
        Integer type = coupon.getCouponType();
        double reduce;
        if (type != null && type == TYPE_DISCOUNT) {
            Double discount = coupon.getCouponDiscount();
            if (discount == null || discount <= 0 || discount >= 10) {
                return 0;
            }
            double payRate = Arith.div(discount, 10, 4);
            reduce = Arith.sub(goodsTotal, Arith.mul(goodsTotal, payRate));
            Double cap = coupon.getMaxReduceAmount();
            if (cap != null && cap > 0 && reduce > cap) {
                reduce = cap;
            }
        } else {
            reduce = coupon.getReduceAmount() == null ? 0 : coupon.getReduceAmount();
        }
        reduce = Arith.round(reduce, 2);
        if (reduce < 0) {
            return 0;
        }
        if (reduce > goodsTotal) {
            return goodsTotal;
        }
        return reduce;
    }

    public static boolean canUse(Coupon coupon, double goodsTotal) {
        return reduce(coupon, goodsTotal) > 0;
    }
}
