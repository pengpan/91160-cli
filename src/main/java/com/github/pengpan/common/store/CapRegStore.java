package com.github.pengpan.common.store;

/**
 * @author pengpan
 */
public class CapRegStore {

    private static String pdId;

    private static String pdKey;

    public static void store(String pdId, String pdKey) {
        CapRegStore.pdId = pdId;
        CapRegStore.pdKey = pdKey;
    }

    public static String getPdId() {
        return pdId;
    }

    public static String getPdKey() {
        return pdKey;
    }
}
