package com.github.pengpan.common.store;

import java.util.List;

/**
 * @author pengpan
 */
public class ProxyStore {

    private static boolean enabled;

    private static List<String> proxyList;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        ProxyStore.enabled = enabled;
    }

    public static List<String> getProxyList() {
        return proxyList;
    }

    public static void setProxyList(List<String> proxyList) {
        ProxyStore.proxyList = proxyList;
    }
}
