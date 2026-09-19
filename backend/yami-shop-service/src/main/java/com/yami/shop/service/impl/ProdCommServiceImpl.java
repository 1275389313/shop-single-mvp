/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */

package com.yami.shop.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.shop.bean.app.dto.ProdCommDataDto;
import com.yami.shop.bean.app.dto.ProdCommDto;
import com.yami.shop.bean.app.param.ProdCommParam;
import com.yami.shop.bean.enums.OrderStatus;
import com.yami.shop.bean.model.Order;
import com.yami.shop.bean.model.OrderItem;
import com.yami.shop.bean.model.ProdComm;
import com.yami.shop.bean.util.ProdCommRules;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.util.Arith;
import com.yami.shop.dao.ProdCommMapper;
import com.yami.shop.service.OrderItemService;
import com.yami.shop.service.OrderService;
import com.yami.shop.service.ProdCommService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Objects;

/**
 * 商品评论
 *
 * @author xwc
 * @date 2019-04-19 10:43:57
 */
@Service
@AllArgsConstructor
public class ProdCommServiceImpl extends ServiceImpl<ProdCommMapper, ProdComm> implements ProdCommService {

    private final ProdCommMapper prodCommMapper;
    private final OrderItemService orderItemService;
    private final OrderService orderService;

    @Override
    public ProdCommDataDto getProdCommDataByProdId(Long prodId) {
        ProdCommDataDto prodCommDataDto=prodCommMapper.getProdCommDataByProdId(prodId);
        if (prodCommDataDto == null) {
            prodCommDataDto = new ProdCommDataDto();
            prodCommDataDto.setNumber(0);
            prodCommDataDto.setPraiseNumber(0);
            prodCommDataDto.setSecondaryNumber(0);
            prodCommDataDto.setNegativeNumber(0);
            prodCommDataDto.setPicNumber(0);
            prodCommDataDto.setPositiveRating(0.0);
            return prodCommDataDto;
        }
        //计算出好评率
        if(prodCommDataDto.getPraiseNumber() == 0||prodCommDataDto.getNumber() == 0){
            prodCommDataDto.setPositiveRating(0.0);
        }else{
            prodCommDataDto.setPositiveRating(Arith.mul(Arith.div(prodCommDataDto.getPraiseNumber(),prodCommDataDto.getNumber()),100));
        }
        return prodCommDataDto;
    }

    @Override
    public IPage<ProdCommDto> getProdCommDtoPageByUserId(Page page, String userId) {
        return prodCommMapper.getProdCommDtoPageByUserId(page,userId);
    }

    @Override
    public IPage<ProdCommDto> getProdCommDtoPageByProdId(Page page, Long prodId, Integer evaluate) {

        IPage<ProdCommDto> prodCommDtos = prodCommMapper.getProdCommDtoPageByProdId(page, prodId, evaluate);
        prodCommDtos.getRecords().forEach(prodCommDto -> {
            // 匿名评价
            if (prodCommDto.getIsAnonymous() != null && prodCommDto.getIsAnonymous() == 1) {
                prodCommDto.setNickName(null);
            }
        });
        return prodCommDtos;
    }

    @Override
    public IPage<ProdComm> getProdCommPage(Page page,ProdComm prodComm) {
        return prodCommMapper.getProdCommPage(page,prodComm);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBuyerReview(String userId, ProdCommParam param) {
        if (!ProdCommRules.validScore(param.getScore())) {
            throw new YamiShopBindException("评分须为1-5分");
        }
        OrderItem orderItem = orderItemService.getById(param.getOrderItemId());
        if (orderItem == null) {
            throw new YamiShopBindException("订单项不存在");
        }
        if (!Objects.equals(userId, orderItem.getUserId())) {
            throw new YamiShopBindException("只能评价自己的订单");
        }
        if (Objects.equals(orderItem.getCommSts(), 1)) {
            throw new YamiShopBindException("该商品已评价");
        }
        long existing = count(new LambdaQueryWrapper<ProdComm>().eq(ProdComm::getOrderItemId, orderItem.getOrderItemId()));
        if (existing > 0) {
            throw new YamiShopBindException("该商品已评价");
        }
        Order order = orderService.getOrderByOrderNumber(orderItem.getOrderNumber());
        if (order == null) {
            throw new YamiShopBindException("订单不存在");
        }
        Integer status = order.getStatus();
        if (!Objects.equals(status, OrderStatus.CONFIRM.value()) && !Objects.equals(status, OrderStatus.SUCCESS.value())) {
            throw new YamiShopBindException("确认收货后才能评价");
        }
        if (param.getProdId() != null && !Objects.equals(param.getProdId(), orderItem.getProdId())) {
            throw new YamiShopBindException("评价商品与订单不符");
        }
        String pics = ProdCommRules.sanitizePics(param.getPics());
        String content = param.getContent() == null ? "" : param.getContent().trim();
        if (!ProdCommRules.hasContentOrPics(content, pics)) {
            throw new YamiShopBindException("请填写评价内容或上传晒图");
        }
        if (content.length() > 500) {
            throw new YamiShopBindException("评价内容过长");
        }

        Integer evaluate = param.getEvaluate();
        if (evaluate == null || evaluate < 0 || evaluate > 2) {
            evaluate = ProdCommRules.evaluateFromScore(param.getScore());
        }

        ProdComm prodComm = new ProdComm();
        prodComm.setProdId(orderItem.getProdId());
        prodComm.setOrderItemId(orderItem.getOrderItemId());
        prodComm.setUserId(userId);
        prodComm.setScore(param.getScore());
        prodComm.setContent(content);
        prodComm.setPics(pics);
        prodComm.setIsAnonymous(Objects.equals(param.getIsAnonymous(), 1) ? 1 : 0);
        prodComm.setRecTime(new Date());
        // MVP: publish immediately so the product page shows the review; admin can hide later.
        prodComm.setStatus(ProdCommRules.STATUS_VISIBLE);
        prodComm.setEvaluate(evaluate);
        prodComm.setReplySts(0);
        prodComm.setUsefulCounts(0);
        save(prodComm);

        orderItemService.update(new LambdaUpdateWrapper<OrderItem>()
                .eq(OrderItem::getOrderItemId, orderItem.getOrderItemId())
                .set(OrderItem::getCommSts, 1));
        orderItemService.removeCacheByOrderNumber(orderItem.getOrderNumber());

        long unreviewed = orderItemService.count(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderNumber, orderItem.getOrderNumber())
                .and(w -> w.isNull(OrderItem::getCommSts).or().eq(OrderItem::getCommSts, 0)));
        if (unreviewed == 0 && Objects.equals(order.getStatus(), OrderStatus.CONFIRM.value())) {
            orderService.update(new LambdaUpdateWrapper<Order>()
                    .eq(Order::getOrderId, order.getOrderId())
                    .eq(Order::getStatus, OrderStatus.CONFIRM.value())
                    .set(Order::getStatus, OrderStatus.SUCCESS.value()));
        }
    }

    @Override
    public void updateStatus(Long prodCommId, Integer status) {
        if (prodCommId == null || !ProdCommRules.validHideStatus(status)) {
            throw new YamiShopBindException("审核状态不正确");
        }
        ProdComm db = getById(prodCommId);
        if (db == null) {
            throw new YamiShopBindException("评论不存在");
        }
        update(new LambdaUpdateWrapper<ProdComm>()
                .eq(ProdComm::getProdCommId, prodCommId)
                .set(ProdComm::getStatus, status));
    }
}
