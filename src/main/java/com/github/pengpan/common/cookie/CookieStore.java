package com.github.pengpan.common.cookie;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.github.pengpan.common.store.AccountStore;
import com.github.pengpan.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Cookie;

import java.util.*;

/**
 * @author pengpan
 */
@Slf4j
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

    public static String accessHash() {
        return getLoginCookieNotNull().value();
    }

    private static Cookie getLoginCookie() {
        List<Cookie> cookies = cookieStore.get("user.91160.com");
        return Optional.ofNullable(cookies).orElseGet(ArrayList::new)
                .stream()
                .sorted(Comparator.comparing(Cookie::expiresAt).reversed())
                .filter(x -> StrUtil.equals("access_hash", x.name())).findFirst()
                .orElse(null);
    }

    public static Cookie getLoginCookieNotNull() {
        Cookie loginCookie = getLoginCookie();
        if (loginCookie == null || System.currentTimeMillis() > (loginCookie.expiresAt() - 60 * 1000)) {
            log.info("登录失效，正在重新登录...");
            clear();
            LoginService loginService = SpringUtil.getBean(LoginService.class);
            loginService.doLogin(AccountStore.getUserName(), AccountStore.getPassword());
            loginCookie = getLoginCookie();
        }
        return loginCookie;
    }

    public static void clear() {
        cookieStore.clear();
    }
}
