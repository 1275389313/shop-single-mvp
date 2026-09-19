package com.yami.shop.bean.util;

import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Buyer product-review rules (mall4j {@code tz_prod_comm}).
 * evaluate: 0 good / 1 medium / 2 poor. status: 1 visible, 0 pending, -1 hidden.
 */
public final class ProdCommRules {

    public static final int EVALUATE_GOOD = 0;
    public static final int EVALUATE_MEDIUM = 1;
    public static final int EVALUATE_POOR = 2;

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_VISIBLE = 1;
    public static final int STATUS_HIDDEN = -1;

    public static final int MAX_PICS = 9;
    public static final int MIN_SCORE = 1;
    public static final int MAX_SCORE = 5;

    private static final Pattern SAFE_RELATIVE = Pattern.compile("^[A-Za-z0-9._\\-/]+$");
    private static final Pattern IMAGE_EXT = Pattern.compile("(?i)\\.(png|jpe?g|gif|webp|svg|bmp)$");

    private ProdCommRules() {
    }

    /**
     * Derive mall4j evaluate from 1–5 star score.
     */
    public static int evaluateFromScore(int score) {
        if (score >= 4) {
            return EVALUATE_GOOD;
        }
        if (score == 3) {
            return EVALUATE_MEDIUM;
        }
        return EVALUATE_POOR;
    }

    public static boolean validScore(Integer score) {
        return score != null && score >= MIN_SCORE && score <= MAX_SCORE;
    }

    public static boolean validHideStatus(Integer status) {
        return status != null && (status == STATUS_VISIBLE || status == STATUS_HIDDEN || status == STATUS_PENDING);
    }

    /**
     * Keep at most {@link #MAX_PICS} comma-separated paths. Allow http(s) URLs or relative upload keys.
     * Reject path traversal and javascript: URLs.
     */
    public static String sanitizePics(String pics) {
        if (StrUtil.isBlank(pics)) {
            return null;
        }
        String[] parts = pics.split(",");
        List<String> kept = new ArrayList<>();
        for (String raw : parts) {
            if (kept.size() >= MAX_PICS) {
                break;
            }
            String item = raw == null ? "" : raw.trim();
            if (item.isEmpty()) {
                continue;
            }
            String lower = item.toLowerCase(Locale.ROOT);
            if (lower.contains("..") || lower.startsWith("javascript:") || lower.startsWith("data:")) {
                continue;
            }
            if (lower.startsWith("http://") || lower.startsWith("https://")) {
                if (IMAGE_EXT.matcher(item).find() || item.contains("/mall4j/img/")) {
                    kept.add(item);
                }
                continue;
            }
            if (SAFE_RELATIVE.matcher(item).matches() && IMAGE_EXT.matcher(item).find()) {
                kept.add(item);
            }
        }
        if (kept.isEmpty()) {
            return null;
        }
        return String.join(",", kept);
    }

    public static boolean hasContentOrPics(String content, String pics) {
        return StrUtil.isNotBlank(content) || StrUtil.isNotBlank(pics);
    }
}
