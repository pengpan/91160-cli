package com.github.pengpan.common.cookie;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pengpan
 */
public class CookieManager implements CookieJar {

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        CookieStore.put(url.host(), cookies);
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> cookies = CookieStore.get(url.host());
        return cookies != null ? cookies : new ArrayList<>();
    }
}
