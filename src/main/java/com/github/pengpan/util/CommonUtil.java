package com.github.pengpan.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.setting.dialect.Props;
import com.github.pengpan.entity.Config;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
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

    public static Config getConfig(String configFile) {
        Assert.notBlank(configFile, "请指定配置文件");
        Assert.isTrue(configFile.endsWith(Props.EXT_NAME), "配置文件不正确");
        File file = FileUtil.file(configFile);
        Assert.isTrue(file.exists(), "配置文件不存在，请检查文件路径");
        Props props = new Props(file, CharsetUtil.CHARSET_UTF_8);
        Config config = new Config();
        props.fillBean(config, null);
        return config;
    }
}
