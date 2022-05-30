package com.github.pengpan.common.proxy;

import cn.hutool.core.util.RandomUtil;
import com.github.pengpan.common.constant.SystemConstant;
import com.github.pengpan.common.store.ProxyStore;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.regex.Matcher;

/**
 * @author pengpan
 */
@Slf4j
public class ProxyPool {

    public static Proxy get() {
        List<String> proxyList = ProxyStore.getProxyList();
        String proxyStr = RandomUtil.randomEle(proxyList);

        Matcher matcher = SystemConstant.PROXY_PATTERN.matcher(proxyStr);
        if (!matcher.matches()) {
            return Proxy.NO_PROXY;
        }

        log.info("代理信息[{}]", proxyStr);

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
