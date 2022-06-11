package com.github.pengpan.interceptor;

import cn.hutool.core.collection.CollUtil;
import com.github.pengpan.common.constant.SystemConstant;
import com.github.pengpan.common.retry.Retry;
import com.github.pengpan.common.retry.RetryRule;
import com.github.pengpan.common.retry.RetryStrategy;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Invocation;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Objects;

/**
 * @author pengpan
 */
@Slf4j
public class RetryInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Invocation invocation = request.tag(Invocation.class);
        Method method = Objects.requireNonNull(invocation).method();
        Retry retry = method.getAnnotation(Retry.class);
        if (retry == null || !retry.enable()) {
            return chain.proceed(request);
        }
        int maxRetries = Math.min(retry.maxRetries(), SystemConstant.LIMIT_RETRIES);
        int intervalMs = retry.intervalMs();
        RetryRule[] retryRules = retry.retryRules();
        return retryIntercept(maxRetries, intervalMs, retryRules, chain);
    }

    private Response retryIntercept(int maxRetries, int intervalMs, RetryRule[] retryRules, Chain chain) {
        HashSet<RetryRule> retryRuleSet = CollUtil.newHashSet(retryRules);
        RetryStrategy retryStrategy = new RetryStrategy(maxRetries, intervalMs);
        while (true) {
            try {
                Request request = chain.request();
                Response response = chain.proceed(request);
                // 如果响应状态码是2xx就不用重试，直接返回 response
                if (!retryRuleSet.contains(RetryRule.RESPONSE_STATUS_NOT_2XX) || response.isSuccessful()) {
                    return response;
                } else {
                    if (!retryStrategy.shouldRetry()) {
                        // 最后一次还没成功，返回最后一次response
                        return response;
                    }
                    // 执行重试
                    retryStrategy.retry();
                    log.debug("The response fails, retry is performed! The response code is " + response.code());
                    response.close();
                }
            } catch (Exception e) {
                if (shouldThrowEx(retryRuleSet, e)) {
                    throw new RuntimeException(e);
                } else {
                    if (!retryStrategy.shouldRetry()) {
                        // 最后一次还没成功，抛出异常
                        throw new RuntimeException("Retry Failed: Total " + maxRetries
                                + " attempts made at interval " + intervalMs
                                + "ms");
                    }
                    retryStrategy.retry();
                }
            }
        }
    }

    private boolean shouldThrowEx(HashSet<RetryRule> retryRuleSet, Exception e) {
        if (retryRuleSet.contains(RetryRule.OCCUR_EXCEPTION)) {
            return false;
        }
        if (retryRuleSet.contains(RetryRule.OCCUR_IO_EXCEPTION)) {
            return !(e instanceof IOException);
        }
        return true;
    }
}
