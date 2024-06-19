package com.github.pengpan.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.setting.dialect.PropsUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * @author pengpan
 */
@Slf4j
public class CommonUtil {

    public static void errorExit(String format, Object... arguments) {
        log.error(format, arguments);
        exit(-1);
    }

    public static void errorExit(String msg, Throwable t) {
        log.error(msg, t);
        exit(-1);
    }

    public static void normalExit(String format, Object... arguments) {
        log.info(format, arguments);
        exit(0);
    }

    public static void normalExit() {
        exit(0);
    }

    public static void exit(int status) {
        boolean isExit = PropsUtil.getSystemProps().getBool("isExit", true);
        if (isExit) {
            System.exit(status);
        }
    }

    public static Date parseDate(String date, String format) {
        try {
            return DateUtil.parse(date, format).toJdkDate();
        } catch (Exception ignored) {
            return null;
        }
    }
}
