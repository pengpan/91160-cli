package com.github.pengpan.common.cookie;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.github.pengpan.common.store.AccountStore;
import com.github.pengpan.service.LoginService;
import com.github.pengpan.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Cookie;

import java.util.*;

/**
 * @author pengpan
 */
@Slf4j
public class CookieStore {

    public static final Map<String, List<Cookie>> cookieStore = new HashMap<>();

    public static void put(String key, Cookie value) {
        put(key, CollUtil.newArrayList(value));
    }

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

    public static List<Cookie> get(String key) {
        return cookieStore.get(key);
    }

    public static void remove(String key) {
        cookieStore.remove(key);
    }

    public static void remove(String key, String name) {
        List<Cookie> oldValue = cookieStore.get(key);
        if (CollUtil.isNotEmpty(oldValue)) {
            List<Cookie> newValue = CollUtil.newArrayList();
            for (Cookie cookie : oldValue) {
                if (!StrUtil.equals(cookie.name(), name)) {
                    newValue.add(cookie);
                }
            }
            cookieStore.put(key, newValue);
        }
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
        if (loginCookieIsExpired(loginCookie)) {
            log.info("登录失效，正在重新登录...");
            clear();
            LoginService loginService = SpringUtil.getBean(LoginService.class);
            int maxLoginRetry = 3;
            for (int i = 0; i < maxLoginRetry; i++) {
                if (loginService.doLogin(AccountStore.getUserName(), AccountStore.getPassword())) {
                    break;
                }
                int sleepMs = RandomUtil.randomInt(1000, 3000);
                log.warn("第{}次登录失败，等待{}毫秒后重试...", i + 1, sleepMs);
                ThreadUtil.sleep(sleepMs);
            }
            loginCookie = getLoginCookie();
            if (loginCookieIsExpired(loginCookie)) {
                CommonUtil.errorExit("连续登录{}次均失败，已终止运行。请先去网页端(https://user.91160.com/login.html)登录成功后再次尝试", maxLoginRetry);
            }
        }
        return loginCookie;
    }

    private static boolean loginCookieIsExpired(Cookie loginCookie) {
        return loginCookie == null || System.currentTimeMillis() > (loginCookie.expiresAt() - 60 * 1000);
    }

    public static void clear() {
        cookieStore.clear();
    }
}
