package com.yami.shop.api.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.bean.app.dto.ProdCommDto;
import com.yami.shop.bean.app.param.ProdCommParam;
import com.yami.shop.bean.model.ProdComm;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.service.ProdCommService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Logged-in buyer reviews. Path is under /p so AuthFilter runs.
 */
@RestController
@RequestMapping("/p/prodComm")
@Tag(name = "我的评价")
@AllArgsConstructor
public class MyProdCommController {

    private final ProdCommService prodCommService;

    @GetMapping("/prodCommPageByUser")
    @Operation(summary = "我的评价分页")
    public ServerResponseEntity<IPage<ProdCommDto>> pageMine(PageParam page) {
        return ServerResponseEntity.success(
                prodCommService.getProdCommDtoPageByUserId(page, SecurityUtils.getUser().getUserId()));
    }

    @PostMapping
    @Operation(summary = "对已收货订单项发表评价（含可选晒图）")
    public ServerResponseEntity<Void> save(@Valid @RequestBody ProdCommParam prodCommParam) {
        prodCommService.saveBuyerReview(SecurityUtils.getUser().getUserId(), prodCommParam);
        return ServerResponseEntity.success();
    }

    @DeleteMapping
    @Operation(summary = "删除自己的评价")
    public ServerResponseEntity<Void> delete(@RequestParam Long prodCommId) {
        String userId = SecurityUtils.getUser().getUserId();
        ProdComm db = prodCommService.getById(prodCommId);
        if (db == null || !userId.equals(db.getUserId())) {
            return ServerResponseEntity.success();
        }
        prodCommService.removeById(prodCommId);
        return ServerResponseEntity.success();
    }
}
