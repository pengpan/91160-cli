package com.github.pengpan.util;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * @author pengpan
 */
@Slf4j
public class CommonUtil {

    public static void errorExit(String format, Object... arguments) {
        log.error(format, arguments);
        System.exit(-1);
    }

    public static void normalExit(String format, Object... arguments) {
        log.info(format, arguments);
        System.exit(0);
    }

    public static Date parseDate(String date, String format) {
        try {
            return DateUtil.parse(date, format).toJdkDate();
        } catch (Exception ignored) {
            return null;
        }
    }
}
