package com.yami.shop.bean.util;

import cn.hutool.crypto.digest.DigestUtil;

/**
 * 快递100即时查询签名：MD5(param + key + customer).toUpperCase()
 */
public final class Kuaidi100Sign {

    private Kuaidi100Sign() {
    }

    public static String sign(String paramJson, String key, String customer) {
        String param = paramJson == null ? "" : paramJson;
        String secret = key == null ? "" : key;
        String cust = customer == null ? "" : customer;
        return DigestUtil.md5Hex(param + secret + cust).toUpperCase();
    }
}
