package com.yami.shop.service.wx;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.yami.shop.common.util.Json;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Calls WeChat subscribe-message HTTP APIs when credentials are present.
 * Never logs AppSecret or access_token. Failures are returned as strings, not thrown.
 * <p>
 * TODO: keyword names in the JSON {@code data} map must match the merchant's template in
 * 微信公众平台 → 订阅消息. A 47003 response means the field list does not match.
 */
@Slf4j
@Component
public class WeChatSubscribeClient {

    private static final int TIMEOUT_MS = 3000;
    private static final String TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";
    private static final String SEND_URL = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send";
    /** Refresh a bit before WeChat's 7200s expiry. */
    private static final long TOKEN_TTL_MS = 7000_000L;

    private final ConcurrentHashMap<String, CachedToken> tokens = new ConcurrentHashMap<>();

    public String send(String appId, String appSecret, String toUser, String templateId, String page,
            String miniprogramState, Map<String, Map<String, String>> data) {
        String token = accessToken(appId, appSecret);
        if (StrUtil.isBlank(token)) {
            return "no access_token";
        }
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("touser", toUser);
        body.put("template_id", templateId);
        if (StrUtil.isNotBlank(page)) {
            body.put("page", page);
        }
        body.put("data", data);
        body.put("miniprogram_state", StrUtil.blankToDefault(miniprogramState, "developer"));
        body.put("lang", "zh_CN");
        String json = Json.toJsonString(body);
        try {
            HttpResponse response = HttpRequest.post(SEND_URL + "?access_token=" + token)
                    .timeout(TIMEOUT_MS)
                    .header("Content-Type", "application/json")
                    .body(json)
                    .execute();
            WxApiResponse parsed = Json.parseObject(response.body(), WxApiResponse.class);
            int err = parsed == null || parsed.getErrcode() == null ? -1 : parsed.getErrcode();
            if (err == 0) {
                log.info("wechat subscribe sent templateLen={} openIdLen={}",
                        templateId.length(), toUser == null ? 0 : toUser.length());
                return "ok";
            }
            log.warn("wechat subscribe send failed errcode={} errmsg={} templateLen={}",
                    err, parsed == null ? "" : StrUtil.blankToDefault(parsed.getErrmsg(), ""),
                    templateId.length());
            return "errcode=" + err;
        } catch (Exception ex) {
            log.warn("wechat subscribe http failed templateLen={}", templateId.length());
            return "http-failed";
        }
    }

    private String accessToken(String appId, String appSecret) {
        CachedToken cached = tokens.get(appId);
        long now = System.currentTimeMillis();
        if (cached != null && cached.expiresAtMs > now) {
            return cached.token;
        }
        try {
            String url = TOKEN_URL
                    + "?grant_type=client_credential&appid=" + URLEncoder.encode(appId, StandardCharsets.UTF_8)
                    + "&secret=" + URLEncoder.encode(appSecret, StandardCharsets.UTF_8);
            HttpResponse response = HttpRequest.get(url)
                    .timeout(TIMEOUT_MS)
                    .execute();
            WxApiResponse parsed = Json.parseObject(response.body(), WxApiResponse.class);
            if (parsed == null || StrUtil.isBlank(parsed.getAccess_token())) {
                Integer err = parsed == null ? null : parsed.getErrcode();
                log.warn("wechat access_token failed errcode={} (secret not logged)", err);
                return null;
            }
            tokens.put(appId, new CachedToken(parsed.getAccess_token(), now + TOKEN_TTL_MS));
            return parsed.getAccess_token();
        } catch (Exception ex) {
            log.warn("wechat access_token http failed appIdLen={}", appId == null ? 0 : appId.length());
            return null;
        }
    }

    private static final class CachedToken {
        private final String token;
        private final long expiresAtMs;

        private CachedToken(String token, long expiresAtMs) {
            this.token = token;
            this.expiresAtMs = expiresAtMs;
        }
    }

    @Data
    public static class WxApiResponse {
        private Integer errcode;
        private String errmsg;
        private String access_token;
        private Integer expires_in;
    }
}
