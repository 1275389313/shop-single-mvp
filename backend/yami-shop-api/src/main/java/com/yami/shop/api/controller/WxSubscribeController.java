package com.yami.shop.api.controller;

import com.yami.shop.bean.dto.SubscribeMessageConfigDto;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.service.SubscribeMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public template IDs for {@code wx.requestSubscribeMessage}. Not under /p, so no login required.
 */
@RestController
@Tag(name = "微信订阅消息")
@RequiredArgsConstructor
public class WxSubscribeController {

    private final SubscribeMessageService subscribeMessageService;

    @GetMapping("/wx/subscribe/config")
    @Operation(summary = "订阅消息模板 ID（可空）", description = "不含 AppSecret。未配置时 data 里的 ID 为空字符串，小程序应跳过授权弹窗。")
    public ServerResponseEntity<SubscribeMessageConfigDto> config() {
        return ServerResponseEntity.success(subscribeMessageService.publicConfig());
    }
}
