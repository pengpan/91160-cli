package com.github.pengpan.common.proxy;

import cn.hutool.core.util.RandomUtil;
import com.github.pengpan.common.store.ProxyStore;
import com.github.pengpan.enums.ProxyModeEnum;
import com.github.pengpan.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.Proxy;
import java.util.List;

/**
 * @author pengpan
 */
@Slf4j
public class ProxyPool {

    private static final ThreadLocal<Integer> currIndex = ThreadLocal.withInitial(() -> 0);

    public static Proxy get() {
        List<String> proxyList = ProxyStore.getProxyList();

        ProxyModeEnum proxyMode = ProxyStore.getProxyMode();
        String proxyStr;
        if (proxyMode == ProxyModeEnum.ROUND_ROBIN) {
            proxyStr = getProxy(proxyList);
        } else if (proxyMode == ProxyModeEnum.RANDOM) {
            proxyStr = RandomUtil.randomEle(proxyList);
        } else {
            return Proxy.NO_PROXY;
        }

        log.info("代理信息[{}]", proxyStr);

        return CommonUtil.getProxy(proxyStr);
    }

    private static String getProxy(List<String> proxyList) {
        int ci = currIndex.get();
        String proxy = proxyList.get(ci);
        currIndex.set((ci + 1) % proxyList.size());
        return proxy;
    }
}
