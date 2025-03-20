package com.github.pengpan.common.proxy;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;

/**
 * @author pengpan
 */
@Slf4j
public class SwitchProxySelector extends ProxySelector {

    public static ThreadLocal<Proxy> proxyThreadLocal = new ThreadLocal<>();

    @Override
    public List<Proxy> select(URI uri) {
        Proxy proxy = SwitchProxySelector.proxyThreadLocal.get();
        if (proxy == null) {
            return null;
        }
        return CollUtil.newArrayList(proxy);
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        log.error("代理连接失败", ioe);
    }
}
