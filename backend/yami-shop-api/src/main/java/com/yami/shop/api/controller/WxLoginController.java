package com.yami.shop.api.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yami.shop.bean.app.param.WxLoginParam;
import com.yami.shop.bean.model.User;
import com.yami.shop.common.config.ShopMvpProperties;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.security.common.bo.UserInfoInTokenBO;
import com.yami.shop.security.common.enums.SysTypeEnum;
import com.yami.shop.security.common.manager.TokenStore;
import com.yami.shop.security.common.vo.TokenInfoVO;
import com.yami.shop.security.common.vo.WxLoginVO;
import com.yami.shop.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * WeChat mini-program login. Mock path is the default for local MVP.
 */
@RestController
@Tag(name = "微信登录")
@RequiredArgsConstructor
public class WxLoginController {

    private final ShopMvpProperties shopMvpProperties;
    private final UserService userService;
    private final TokenStore tokenStore;

    @PostMapping("/wx/login")
    @Operation(summary = "微信登录（mock 可关）", description = "shop-mvp.mock.wechat-login=true 时把 code 当作 openId，不请求微信。")
    public ServerResponseEntity<WxLoginVO> login(@Valid @RequestBody WxLoginParam param) {
        String openId = resolveOpenId(param.getCode());
        Date now = new Date();
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getWxOpenId, openId));
        if (user == null) {
            user = new User();
            user.setUserId(IdUtil.simpleUUID());
            user.setWxOpenId(openId);
            user.setNickName(StrUtil.blankToDefault(param.getNickName(), "微信用户"));
            user.setStatus(1);
            user.setUserRegtime(now);
            user.setModifyTime(now);
            user.setPic("");
            userService.save(user);
        } else {
            user.setUserLasttime(now);
            user.setModifyTime(now);
            if (StrUtil.isNotBlank(param.getNickName())) {
                user.setNickName(param.getNickName());
            }
            userService.updateById(user);
        }

        UserInfoInTokenBO tokenUser = new UserInfoInTokenBO();
        tokenUser.setUserId(user.getUserId());
        tokenUser.setNickName(user.getNickName());
        tokenUser.setSysType(SysTypeEnum.ORDINARY.value());
        tokenUser.setIsAdmin(0);
        tokenUser.setEnabled(user.getStatus() != null && user.getStatus() == 1);
        tokenUser.setShopId(1L);
        TokenInfoVO token = tokenStore.storeAndGetVo(tokenUser);

        WxLoginVO vo = new WxLoginVO();
        vo.setAccessToken(token.getAccessToken());
        vo.setRefreshToken(token.getRefreshToken());
        vo.setExpiresIn(token.getExpiresIn());
        vo.setUserId(user.getUserId());
        vo.setNickName(user.getNickName());
        vo.setPic(user.getPic());
        vo.setMock(shopMvpProperties.getMock().isWechatLogin());
        return ServerResponseEntity.success(vo);
    }

    private String resolveOpenId(String code) {
        if (shopMvpProperties.getMock().isWechatLogin()) {
            return "mock_" + code;
        }
        if (StrUtil.isBlank(shopMvpProperties.getWx().getAppId())
                || StrUtil.isBlank(shopMvpProperties.getWx().getAppSecret())) {
            throw new YamiShopBindException("未配置 WX_APP_ID / WX_APP_SECRET。本地请保持 shop-mvp.mock.wechat-login=true");
        }
        // TODO: call WeChat jscode2session with appId+secret+code, then persist real openid
        throw new YamiShopBindException("真实微信登录未接入（TODO: code2session）。请先使用 mock 登录");
    }
}
