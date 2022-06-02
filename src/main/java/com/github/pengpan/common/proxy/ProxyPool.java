package com.github.pengpan.common.proxy;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;

import com.github.pengpan.common.constant.SystemConstant;
import com.github.pengpan.common.store.ProxyStore;
import com.github.pengpan.enums.ProxyModeEnum;

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

    private static ThreadLocal<Integer> currIndex = ThreadLocal.withInitial(() -> 0);

    public static Proxy get() {
        List<String> proxyList = ProxyStore.getProxyList();

        ProxyModeEnum proxyMode = ProxyStore.getProxyMode();
        String proxyStr;
        if (proxyMode == ProxyModeEnum.ROUND_ROBIN) {
            proxyStr = getProxy(proxyList);
        } else if (proxyMode == ProxyModeEnum.RANDOM) {
            proxyStr = RandomUtil.randomEle(proxyList);
        } else {
            proxyStr = StrUtil.EMPTY; 
        }

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

    private static String getProxy(List<String> proxyList) {
        int ci = currIndex.get();
        String proxy = proxyList.get(ci);
        currIndex.set((ci + 1) % proxyList.size());
        return proxy;
    }
}
