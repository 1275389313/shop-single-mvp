package com.yami.shop.service.express;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.yami.shop.bean.app.dto.DeliveryDto;
import com.yami.shop.bean.app.dto.DeliveryInfoDto;
import com.yami.shop.bean.util.Kuaidi100Sign;
import com.yami.shop.bean.util.MockExpressTrack;
import com.yami.shop.common.config.ShopMvpProperties;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.util.Json;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Official 快递100 poll query. Never logs customer/key.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Kuaidi100ExpressQueryClient implements ExpressQueryClient {

    private static final int TIMEOUT_MS = 8000;

    private final ShopMvpProperties shopMvpProperties;

    @Override
    public DeliveryDto query(String companyCode, String trackingNo, String phone) {
        ShopMvpProperties.Kuaidi100 conf = shopMvpProperties.getKuaidi100();
        if (conf == null || !conf.isConfigured()) {
            throw new YamiShopBindException("未配置快递100密钥");
        }
        if (StrUtil.isBlank(companyCode)) {
            throw new YamiShopBindException("无法识别快递公司编码，无法查询快递100");
        }
        Map<String, Object> param = new LinkedHashMap<>();
        param.put("com", companyCode);
        param.put("num", trackingNo);
        if (StrUtil.isNotBlank(phone)) {
            param.put("phone", phone);
        }
        param.put("resultv2", "1");
        String paramJson = Json.toJsonString(param);
        String sign = Kuaidi100Sign.sign(paramJson, conf.getKey(), conf.getCustomer());

        String body;
        try {
            HttpResponse response = HttpRequest.post(conf.getQueryUrl())
                    .timeout(TIMEOUT_MS)
                    .form("customer", conf.getCustomer())
                    .form("sign", sign)
                    .form("param", paramJson)
                    .execute();
            body = response.body();
        } catch (Exception e) {
            log.warn("kuaidi100 http failed company={} trackingLen={}", companyCode,
                    trackingNo == null ? 0 : trackingNo.length());
            throw new YamiShopBindException("快递100请求失败，请稍后重试");
        }

        Kuaidi100Response parsed = Json.parseObject(body, Kuaidi100Response.class);
        if (parsed == null) {
            throw new YamiShopBindException("快递100返回无法解析");
        }
        DeliveryDto dto = new DeliveryDto();
        dto.setMock(Boolean.FALSE);
        dto.setSource("kuaidi100");
        dto.setCompanyCode(StrUtil.blankToDefault(parsed.getCom(), companyCode));
        dto.setDvyFlowId(StrUtil.blankToDefault(parsed.getNu(), trackingNo));
        dto.setState(parsed.getState());
        dto.setStateText(MockExpressTrack.stateText(parsed.getState()));
        dto.setMessage(StrUtil.blankToDefault(parsed.getMessage(), ""));
        List<DeliveryInfoDto> traces = parsed.getData();
        dto.setData(traces == null ? Collections.emptyList() : traces);
        String status = parsed.getStatus();
        if (StrUtil.isNotBlank(status) && !"200".equals(status) && !"201".equals(status)) {
            throw new YamiShopBindException(StrUtil.blankToDefault(parsed.getMessage(), "快递100查询失败 status=" + status));
        }
        return dto;
    }

    @Data
    public static class Kuaidi100Response {
        private String message;
        private String nu;
        private String com;
        private String status;
        private String state;
        private List<DeliveryInfoDto> data;
    }
}
