package com.yami.shop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.shop.bean.app.dto.CouponDto;
import com.yami.shop.bean.app.dto.ShopCartOrderDto;
import com.yami.shop.bean.app.param.OrderParam;
import com.yami.shop.bean.model.Coupon;
import com.yami.shop.common.util.PageParam;

/**
 * 优惠券模板 + 结算/核销。
 */
public interface CouponService extends IService<Coupon> {

    void validateAndFill(Coupon coupon);

    IPage<CouponDto> pageCenter(PageParam<CouponDto> page);

    void receive(String userId, Long couponId);

    IPage<CouponDto> pageMine(String userId, Integer status, PageParam<CouponDto> page);

    /**
     * 确认订单：列出用户券、校验选择、写入减免。
     */
    void applyToConfirm(ShopCartOrderDto shopCartOrderDto, OrderParam orderParam, String userId);

    /**
     * 提交订单：核销已选券。
     */
    void useOnSubmit(ShopCartOrderDto shopCartOrderDto, String userId);

    /**
     * 未支付取消：退回用户券。
     */
    void restoreByOrderNumber(String orderNumber);
}
