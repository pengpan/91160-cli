package com.github.pengpan.common.store;

/**
 * @author pengpan
 */
public class DdddOcrStore {

    private static String baseUrl;

    public static void store(String baseUrl) {
        DdddOcrStore.baseUrl = baseUrl;
    }

    public static String getBaseUrl() {
        return baseUrl;
    }
}
