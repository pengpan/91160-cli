package com.github.pengpan.common.store;

import cn.hutool.core.util.StrUtil;

/**
 * @author pengpan
 */
public class AccountStore {

    private static String userName;

    private static String password;

    public static void store(String userName, String password) {
        AccountStore.userName = userName;
        AccountStore.password = password;
    }

    public static void clean() {
        AccountStore.userName = null;
        AccountStore.password = null;
    }

    public static boolean isStore() {
        return StrUtil.isNotBlank(userName) && StrUtil.isNotBlank(password);
    }

    public static String getUserName() {
        return userName;
    }

    public static String getPassword() {
        return password;
    }
}
