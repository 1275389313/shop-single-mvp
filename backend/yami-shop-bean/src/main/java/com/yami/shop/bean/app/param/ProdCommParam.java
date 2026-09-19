/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */

package com.yami.shop.bean.app.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * @author lanhai
 */
@Data
@Schema(description = "添加评论信息")
public class ProdCommParam {
    /**
     * 商品ID
     */
    @Schema(description = "商品id")
    private Long prodId;
    /**
     * 订单项ID
     */
    @Schema(description = "订单项ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "订单项不能为空")
    private Long orderItemId;

    /**
     * 评价，0-5分
     */
    @Schema(description = "评价，1-5分" ,requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "评价不能为空")
    @Min(value = 1, message = "评分最少1分")
    @Max(value = 5, message = "评分最多5分")
    private Integer score;

    @Schema(description = "评论内容" ,requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "评论图片, 用逗号分隔" )
    private String pics;

    /**
     * 是否匿名(1:是  0:否)
     */
    @Schema(description = "是否匿名(1:是  0:否) 默认为否" )
    private Integer isAnonymous;


    @Schema(description = "* 评价(0好评 1中评 2差评)" )
    private Integer evaluate;

}
