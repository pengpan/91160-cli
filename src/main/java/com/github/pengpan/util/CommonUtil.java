package com.github.pengpan.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.setting.dialect.PropsUtil;
import com.github.pengpan.common.constant.SystemConstant;
import lombok.extern.slf4j.Slf4j;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.Date;
import java.util.regex.Matcher;

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

    public static boolean validateProxy(Proxy proxy) {
        try {
            URL url = new URL("https://www.baidu.com");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static Proxy getProxy(String proxyStr) {
        Matcher matcher = SystemConstant.PROXY_PATTERN.matcher(proxyStr);
        if (!matcher.matches()) {
            return Proxy.NO_PROXY;
        }

        String model = matcher.group(1);
        String host = matcher.group(2);
        int port = Integer.parseInt(matcher.group(3));

        switch (model) {
            case "socks":
                return new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(host, port));
            case "http":
                return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
        }
        return Proxy.NO_PROXY;
    }
}
