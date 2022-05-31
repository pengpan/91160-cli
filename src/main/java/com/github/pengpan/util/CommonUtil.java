package com.github.pengpan.util;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * @author pengpan
 */
@Slf4j
public class CommonUtil {

    public static void errorExit(String message) {
        log.error(message);
        System.exit(-1);
    }

    public static void normalExit(String message) {
        log.info(message);
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
