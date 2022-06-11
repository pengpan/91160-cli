package com.github.pengpan.interceptor;

import com.github.pengpan.common.proxy.Proxy;
import com.github.pengpan.common.proxy.ProxyPool;
import com.github.pengpan.common.proxy.SwitchProxySelector;
import com.github.pengpan.common.store.ProxyStore;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Invocation;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author pengpan
 */
@Slf4j
public class ProxyInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (!ProxyStore.isEnabled()) {
            return chain.proceed(request);
        }
        Invocation invocation = request.tag(Invocation.class);
        Method method = Objects.requireNonNull(invocation).method();
        Proxy proxy = method.getAnnotation(Proxy.class);
        if (proxy != null && proxy.enable()) {
            SwitchProxySelector.proxyThreadLocal.set(ProxyPool.get());
        }
        return chain.proceed(request);
    }
}
