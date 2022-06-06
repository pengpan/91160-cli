package com.github.pengpan.common.retry;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author pengpan
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface Retry {

    /**
     * 是否启用重试
     */
    boolean enable() default true;

    /**
     * 最大重试次数，最大可设置为100
     */
    int maxRetries() default 2;

    /**
     * 重试时间间隔
     */
    int intervalMs() default 100;

    /**
     * 重试规则，默认 响应状态码不是2xx 或者 发生IO异常 时触发重试
     */
    RetryRule[] retryRules() default {RetryRule.RESPONSE_STATUS_NOT_2XX, RetryRule.OCCUR_IO_EXCEPTION};

}
