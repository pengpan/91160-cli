package com.github.pengpan.common.store;

import java.util.List;

import com.github.pengpan.enums.ProxyModeEnum;

/**
 * @author pengpan
 */
public class ProxyStore {

    private static boolean enabled;

    private static List<String> proxyList;

    private static ProxyModeEnum proxyMode;

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

    public static ProxyModeEnum getProxyMode() {
        return proxyMode;
    }

    public static void setProxyMode(ProxyModeEnum proxyMode) {
        ProxyStore.proxyMode = proxyMode;
    }
}
