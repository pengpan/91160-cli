package com.github.pengpan.util;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author pengpan
 */
@Slf4j
public class Assert {

    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            CommonUtil.errorExit(message);
        }
    }

    public static void notTrue(boolean expression, String message) {
        if (expression) {
            CommonUtil.errorExit(message);
        }
    }

    public static <T> T isNull(T obj, String message) {
        if (obj != null) {
            CommonUtil.errorExit(message);
        }
        return obj;
    }

    public static <T> T notNull(T obj, String message) {
        if (obj == null) {
            CommonUtil.errorExit(message);
        }
        return obj;
    }

    public static void isBlank(String str, String message) {
        if (StrUtil.isNotBlank(str)) {
            CommonUtil.errorExit(message);
        }
    }

    public static void notBlank(String str, String message) {
        if (StrUtil.isBlank(str)) {
            CommonUtil.errorExit(message);
        }
    }
}
