package com.github.pengpan.interceptor;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.IOException;

/**
 * @author pengpan
 */
@Slf4j
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
            logger = log::info;
        }
        httpLoggingInterceptor = new HttpLoggingInterceptor(logger);
        httpLoggingInterceptor.setLevel(level);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        return httpLoggingInterceptor.intercept(chain);
    }
}
