package com.github.pengpan.common.cookie;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import okhttp3.Cookie;

import java.util.*;

public class CookieStore {

    public static final Map<String, List<Cookie>> cookieStore = new HashMap<>();

    public static void put(String key, List<Cookie> value) {
        List<Cookie> oldValue = cookieStore.get(key);
        if (CollUtil.isEmpty(oldValue)) {
            cookieStore.put(key, value);
        } else {
            List<Cookie> newValue = CollUtil.newArrayList();
            newValue.addAll(oldValue);
            newValue.addAll(value);
            cookieStore.put(key, newValue);
        }
    }

    public static List<Cookie> get(String name) {
        return cookieStore.get(name);
    }

    public static boolean isLogin() {
        Cookie loginCookie = getLoginCookie();
        return loginCookie != null && loginCookie.expiresAt() > System.currentTimeMillis();
    }

    public static String accessHash() {
        Cookie loginCookie = getLoginCookie();
        if (loginCookie == null) {
            throw new RuntimeException("请先登录");
        }
        if (loginCookie.expiresAt() < System.currentTimeMillis()) {
            throw new RuntimeException("会话失效，请重新登录");
        }
        return loginCookie.value();
    }

    private static Cookie getLoginCookie() {
        List<Cookie> cookies = cookieStore.get("user.91160.com");
        return Optional.ofNullable(cookies).orElseGet(ArrayList::new)
                .stream()
                .filter(x -> StrUtil.equals("access_hash", x.name())).findFirst()
                .orElse(null);
    }

}
