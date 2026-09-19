/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */

package com.yami.shop.bean.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author lanhai
 */
@Data
public class DeliveryDto {

	@Schema(description = "物流公司名称" ,requiredMode = Schema.RequiredMode.REQUIRED)
	private String companyName;
	
	@Schema(description = "物流公司官网" ,requiredMode = Schema.RequiredMode.REQUIRED)
	private String companyHomeUrl;
	
	@Schema(description = "物流订单号" ,requiredMode = Schema.RequiredMode.REQUIRED)
	private String dvyFlowId;
	
	@Schema(description = "查询出的物流信息" ,requiredMode = Schema.RequiredMode.REQUIRED)
	private List<DeliveryInfoDto> data;

	@Schema(description = "快递100状态码：0在途 1揽收 3已签收 5派件等")
	private String state;

	@Schema(description = "状态文案")
	private String stateText;

	@Schema(description = "true 表示未配密钥，返回的是模拟轨迹")
	private Boolean mock;

	@Schema(description = "数据来源：mock / kuaidi100")
	private String source;

	@Schema(description = "SHIPMENT 正向发货 / RETURN 退货")
	private String direction;

	@Schema(description = "说明（模拟提示、尚未发货、快递100原文等）")
	private String message;

	@Schema(description = "快递100公司编码，如 shunfeng")
	private String companyCode;

}
