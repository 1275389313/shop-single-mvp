/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */

package com.yami.shop.admin.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.bean.model.ShopDetail;
import com.yami.shop.bean.param.ShopDetailParam;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.security.admin.util.SecurityUtils;
import com.yami.shop.service.ShopDetailService;
import cn.hutool.core.bean.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import com.yami.shop.common.response.ServerResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;



/**
 *
 * @author lgh on 2018/08/29.
 */
@RestController
@RequestMapping("/shop/shopDetail")
public class ShopDetailController {

    @Autowired
    private ShopDetailService shopDetailService;


	/**
	 * 修改分销开关
	 */
	@PutMapping("/isDistribution")
	public ServerResponseEntity<Void> updateIsDistribution(@RequestParam Integer isDistribution){
		ShopDetail shopDetail=new ShopDetail();
		shopDetail.setShopId(SecurityUtils.getSysUser().getShopId());
		shopDetail.setIsDistribution(isDistribution);
		shopDetailService.updateById(shopDetail);
		// 更新完成后删除缓存
		shopDetailService.removeShopDetailCacheByShopId(shopDetail.getShopId());
		return ServerResponseEntity.success();
	}
	/**
	 * 获取信息
	 */
	@GetMapping("/info")
	@PreAuthorize("@pms.hasPermission('shop:shopDetail:info')")
	public ServerResponseEntity<ShopDetail> info(){
		ShopDetail shopDetail = shopDetailService.getShopDetailByShopId(SecurityUtils.getSysUser().getShopId());
		return ServerResponseEntity.success(shopDetail);
	}


	/**
	 * 分页获取（单店：只返回当前登录店铺）
	 */
    @GetMapping("/page")
	@PreAuthorize("@pms.hasPermission('shop:shopDetail:page')")
	public ServerResponseEntity<IPage<ShopDetail>> page(ShopDetail shopDetail,PageParam<ShopDetail> page){
		Long shopId = currentShopId();
		IPage<ShopDetail> shopDetails = shopDetailService.page(page,
				new LambdaQueryWrapper<ShopDetail>()
						.eq(ShopDetail::getShopId, shopId)
						.like(StrUtil.isNotBlank(shopDetail.getShopName()),ShopDetail::getShopName,shopDetail.getShopName())
						.orderByDesc(ShopDetail::getShopId));
		return ServerResponseEntity.success(shopDetails);
	}

	/**
	 * 获取信息
	 */
	@GetMapping("/info/{shopId}")
	@PreAuthorize("@pms.hasPermission('shop:shopDetail:info')")
	public ServerResponseEntity<ShopDetail> info(@PathVariable("shopId") Long shopId){
		assertCurrentShop(shopId);
		ShopDetail shopDetail = shopDetailService.getShopDetailByShopId(shopId);
		return ServerResponseEntity.success(shopDetail);
	}

	/**
	 * 保存 — 单店版禁用创建新店铺
	 */
	@PostMapping
	@PreAuthorize("@pms.hasPermission('shop:shopDetail:save')")
	public ServerResponseEntity<Void> save(@Valid ShopDetailParam shopDetailParam){
		throw new YamiShopBindException("单店版已禁用新建店铺，请修改当前店铺资料");
	}

	/**
	 * 修改（强制落在当前店铺）
	 */
	@PutMapping
	@PreAuthorize("@pms.hasPermission('shop:shopDetail:update')")
	public ServerResponseEntity<Void> update(@Valid ShopDetailParam shopDetailParam){
		Long shopId = currentShopId();
		shopDetailParam.setShopId(shopId);
		ShopDetail daShopDetail = shopDetailService.getShopDetailByShopId(shopId);
		ShopDetail shopDetail = BeanUtil.copyProperties(shopDetailParam, ShopDetail.class);
		shopDetail.setShopId(shopId);
		shopDetail.setUpdateTime(new Date());
		shopDetailService.updateShopDetail(shopDetail,daShopDetail);
		return ServerResponseEntity.success();
	}

	/**
	 * 删除 — 单店版禁用
	 */
	@DeleteMapping("/{id}")
	@PreAuthorize("@pms.hasPermission('shop:shopDetail:delete')")
	public ServerResponseEntity<Void> delete(@PathVariable Long id){
		throw new YamiShopBindException("单店版已禁用删除店铺");
	}

	/**
	 * 更新店铺状态 — 仅当前店铺
	 */
	@PutMapping("/shopStatus")
	@PreAuthorize("@pms.hasPermission('shop:shopDetail:shopStatus')")
	public ServerResponseEntity<Void> shopStatus(@RequestParam Long shopId,@RequestParam Integer shopStatus){
		assertCurrentShop(shopId);
		ShopDetail shopDetail = new ShopDetail();
		shopDetail.setShopId(shopId);
		shopDetail.setShopStatus(shopStatus);
		shopDetailService.updateById(shopDetail);
		shopDetailService.removeShopDetailCacheByShopId(shopDetail.getShopId());
		return ServerResponseEntity.success();
	}


	/**
	 * 获取店铺名称（单店只返回自己）
	 */
    @GetMapping("/listShopName")
	public ServerResponseEntity<List<ShopDetail>> listShopName(){
		Long shopId = currentShopId();
		List<ShopDetail> list = shopDetailService.list(new LambdaQueryWrapper<ShopDetail>().eq(ShopDetail::getShopId, shopId))
				.stream().map((dbShopDetail) ->{
			ShopDetail shopDetail = new ShopDetail();
			shopDetail.setShopId(dbShopDetail.getShopId());
			shopDetail.setShopName(dbShopDetail.getShopName());
			return shopDetail;
		}).collect(Collectors.toList());
		return ServerResponseEntity.success(list);
	}

	private Long currentShopId() {
		Long shopId = SecurityUtils.getSysUser().getShopId();
		return shopId == null ? 1L : shopId;
	}

	private void assertCurrentShop(Long shopId) {
		if (shopId == null || !java.util.Objects.equals(shopId, currentShopId())) {
			throw new YamiShopBindException("单店版不能操作其他店铺");
		}
	}
}
