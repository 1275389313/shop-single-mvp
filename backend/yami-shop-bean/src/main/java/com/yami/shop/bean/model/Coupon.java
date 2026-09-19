package com.yami.shop.bean.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 优惠券模板（开源 mall4j 无此表，本仓库 P1 补齐）。
 */
@Data
@TableName("tz_coupon")
public class Coupon implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId
    private Long couponId;

    private Long shopId;

    private String couponName;

    private String subTitle;

    /**
     * 1 满减 2 折扣
     */
    private Integer couponType;

    /**
     * 使用门槛，满多少可用，0 无门槛
     */
    private Double cashCondition;

    /**
     * 满减金额
     */
    private Double reduceAmount;

    /**
     * 折扣（折），如 8.5 表示 8.5 折
     */
    private Double couponDiscount;

    /**
     * 折扣券最多减免，null/0 不封顶
     */
    private Double maxReduceAmount;

    /**
     * 0 全部商品
     */
    private Integer suitableProdType;

    /**
     * 剩余库存，-1 不限
     */
    private Integer stocks;

    private Integer sourceStock;

    /**
     * 每人限领，-1 不限
     */
    private Integer limitNum;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    /**
     * 1 领取后 N 天 2 固定时间段
     */
    private Integer validTimeType;

    private Integer validDays;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date validStartTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date validEndTime;

    /**
     * 0 下线 1 投放
     */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
