package com.github.pengpan.interceptor;

import okhttp3.Interceptor;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.IOException;

/**
 * @author pengpan
 */
public class LoggingInterceptor implements Interceptor {

    private HttpLoggingInterceptor httpLoggingInterceptor;

    public LoggingInterceptor() {
        this(null, null);
    }

    public LoggingInterceptor(HttpLoggingInterceptor.Level level, HttpLoggingInterceptor.Logger logger) {
        if (level == null) {
            level = HttpLoggingInterceptor.Level.BASIC;
        }
        if (logger == null) {
            logger = System.out::println;
        }
        httpLoggingInterceptor = new HttpLoggingInterceptor(logger);
        httpLoggingInterceptor.setLevel(level);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        return httpLoggingInterceptor.intercept(chain);
    }
}
