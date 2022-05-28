package com.github.pengpan.common.util;

import lombok.extern.slf4j.Slf4j;

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
}
