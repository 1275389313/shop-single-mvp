package com.yami.shop.bean.app.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 领券中心 / 我的优惠券。
 */
@Data
public class CouponDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "优惠券模板ID")
    private Long couponId;

    @Schema(description = "用户优惠券ID，我的优惠券才有")
    private Long couponUserId;

    private String couponName;

    private String subTitle;

    @Schema(description = "1满减 2折扣")
    private Integer couponType;

    private Double cashCondition;

    private Double reduceAmount;

    private Double couponDiscount;

    private Double maxReduceAmount;

    private Integer stocks;

    private Integer limitNum;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date userStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date userEndTime;

    @Schema(description = "0未使用 1已使用 2已过期")
    private Integer status;

    @Schema(description = "是否还能领取（领券中心）")
    private Boolean canReceive;
}
