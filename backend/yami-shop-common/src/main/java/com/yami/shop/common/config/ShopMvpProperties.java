package com.yami.shop.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Phase-1 MVP switches for single-store WeChat mock login/pay and order jobs.
 */
@Data
@ConfigurationProperties(prefix = "shop-mvp")
public class ShopMvpProperties {

    private Mock mock = new Mock();

    private Order order = new Order();

    private Wx wx = new Wx();

    @Data
    public static class Mock {
        /**
         * Skip WeChat code2session; treat incoming code as an openId.
         */
        private boolean wechatLogin = true;

        /**
         * Skip WeChat pay SDK; mark order paid in-process or via /notice/pay/mock.
         */
        private boolean pay = true;
    }

    @Data
    public static class Order {
        private boolean autoCloseEnabled = true;
        private int autoCloseMinutes = 30;
        private int autoConfirmDays = 15;
    }

    @Data
    public static class Wx {
        /**
         * Mini-program AppID placeholder. Required when mock.wechat-login=false.
         */
        private String appId = "";
        private String appSecret = "";
        private String payMchId = "";
        private String payApiKey = "";
        /**
         * Public HTTPS notify URL for real WeChat pay callbacks.
         */
        private String payNotifyUrl = "";
    }
}
