package com.github.pengpan.util;

import cn.hutool.core.collection.CollUtil;
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

    public static void isNull(Object obj, String message) {
        if (obj != null) {
            CommonUtil.errorExit(message);
        }
    }

    public static void notNull(Object obj, String message) {
        if (obj == null) {
            CommonUtil.errorExit(message);
        }
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

    public static void isEmpty(Iterable<?> iterable, String message) {
        if (CollUtil.isNotEmpty(iterable)) {
            CommonUtil.errorExit(message);
        }
    }

    public static void notEmpty(Iterable<?> iterable, String message) {
        if (CollUtil.isEmpty(iterable)) {
            CommonUtil.errorExit(message);
        }
    }
}
