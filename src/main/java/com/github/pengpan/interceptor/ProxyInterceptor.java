package com.github.pengpan.interceptor;

import com.github.pengpan.common.proxy.EnableProxy;
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
import java.net.Proxy;

/**
 * @author pengpan
 */
@Slf4j
public class ProxyInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Invocation invocation = request.tag(Invocation.class);
        assert invocation != null;
        Method method = invocation.method();
        EnableProxy enableProxy = method.getAnnotation(EnableProxy.class);
        if (enableProxy != null && ProxyStore.isEnabled()) {
            Proxy proxy = ProxyPool.get();
            SwitchProxySelector.proxyThreadLocal.set(proxy);
        }
        return chain.proceed(request);
    }
}
