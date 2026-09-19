package com.yami.shop.bean.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Kuaidi100SignTest {

    @Test
    void md5UppercaseOfParamPlusKeyPlusCustomer() {
        String expected = cn.hutool.crypto.digest.DigestUtil.md5Hex("paramkeycust").toUpperCase();
        assertEquals(expected, Kuaidi100Sign.sign("param", "key", "cust"));
        assertEquals(32, expected.length());
        assertEquals(expected, expected.toUpperCase());
    }
}
