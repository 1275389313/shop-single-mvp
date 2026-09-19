package com.yami.shop.bean.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户领取的优惠券。
 */
@Data
@TableName("tz_coupon_user")
public class CouponUser implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public static final int STS_UNUSED = 0;
    public static final int STS_USED = 1;
    public static final int STS_EXPIRED = 2;

    @TableId
    private Long couponUserId;

    private Long couponId;

    private String userId;

    /**
     * 0未使用 1已使用 2已过期
     */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date receiveTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date userStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date userEndTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date useTime;

    private String orderNumber;

    @TableField(exist = false)
    private Coupon coupon;
}
