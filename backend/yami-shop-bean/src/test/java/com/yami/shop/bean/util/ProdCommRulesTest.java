package com.yami.shop.bean.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProdCommRulesTest {

    @Test
    void evaluateFromScore() {
        assertEquals(ProdCommRules.EVALUATE_GOOD, ProdCommRules.evaluateFromScore(5));
        assertEquals(ProdCommRules.EVALUATE_GOOD, ProdCommRules.evaluateFromScore(4));
        assertEquals(ProdCommRules.EVALUATE_MEDIUM, ProdCommRules.evaluateFromScore(3));
        assertEquals(ProdCommRules.EVALUATE_POOR, ProdCommRules.evaluateFromScore(2));
        assertEquals(ProdCommRules.EVALUATE_POOR, ProdCommRules.evaluateFromScore(1));
    }

    @Test
    void validScore() {
        assertFalse(ProdCommRules.validScore(null));
        assertFalse(ProdCommRules.validScore(0));
        assertFalse(ProdCommRules.validScore(6));
        assertTrue(ProdCommRules.validScore(1));
        assertTrue(ProdCommRules.validScore(5));
    }

    @Test
    void sanitizePicsKeepsRelativeAndHttp() {
        assertEquals("2026/09/a.jpg,http://127.0.0.1:8086/mall4j/img/b.png",
                ProdCommRules.sanitizePics(" 2026/09/a.jpg , http://127.0.0.1:8086/mall4j/img/b.png "));
    }

    @Test
    void sanitizePicsDropsTraversalAndDataUri() {
        assertNull(ProdCommRules.sanitizePics("../etc/passwd"));
        assertNull(ProdCommRules.sanitizePics("javascript:alert(1)"));
        assertNull(ProdCommRules.sanitizePics("data:image/png;base64,xxxx"));
        assertEquals("ok.jpg", ProdCommRules.sanitizePics("ok.jpg,../x.jpg"));
    }

    @Test
    void sanitizePicsCapsAtNine() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(i).append(".jpg");
        }
        String out = ProdCommRules.sanitizePics(sb.toString());
        assertEquals(9, out.split(",").length);
    }

    @Test
    void hasContentOrPics() {
        assertTrue(ProdCommRules.hasContentOrPics("nice", null));
        assertTrue(ProdCommRules.hasContentOrPics(" ", "a.jpg"));
        assertFalse(ProdCommRules.hasContentOrPics("  ", null));
        assertFalse(ProdCommRules.hasContentOrPics(null, " "));
    }
}
