package com.github.pengpan.config;

import com.alibaba.fastjson.support.retrofit.Retrofit2ConverterFactory;
import com.github.pengpan.client.MainClient;
import com.github.pengpan.common.constant.SystemConstant;
import com.github.pengpan.common.cookie.CookieManager;
import com.github.pengpan.common.proxy.SwitchProxySelector;
import com.github.pengpan.common.retrofit.BasicTypeConverterFactory;
import com.github.pengpan.common.retrofit.BodyCallAdapterFactory;
import com.github.pengpan.common.retrofit.ResponseCallAdapterFactory;
import com.github.pengpan.interceptor.LoggingInterceptor;
import com.github.pengpan.interceptor.MainClientInterceptor;
import com.github.pengpan.interceptor.ProxyInterceptor;
import com.github.pengpan.interceptor.RetryInterceptor;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;

import java.util.concurrent.TimeUnit;

/**
 * @author pengpan
 */
@Configuration
public class RetrofitConfiguration {

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(new ProxyInterceptor())
                .addInterceptor(new MainClientInterceptor())
                .addInterceptor(new LoggingInterceptor())
                .addInterceptor(new RetryInterceptor())
                .proxySelector(new SwitchProxySelector())
                .followRedirects(false)
                .cookieJar(new CookieManager())
                .connectionPool(new ConnectionPool(200, 2, TimeUnit.MINUTES))
                .connectTimeout(10000, TimeUnit.MILLISECONDS)
                .readTimeout(10000, TimeUnit.MILLISECONDS)
                .writeTimeout(10000, TimeUnit.MILLISECONDS)
                .build();
    }

    @Bean
    public Retrofit retrofit(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(SystemConstant.DOMAIN)
                .client(okHttpClient)
                .addCallAdapterFactory(new BodyCallAdapterFactory())
                .addCallAdapterFactory(new ResponseCallAdapterFactory())
                .addConverterFactory(new BasicTypeConverterFactory())
                .addConverterFactory(Retrofit2ConverterFactory.create())
                .build();
    }

    @Bean
    public MainClient mainClient(Retrofit retrofit) {
        return retrofit.create(MainClient.class);
    }
}
