package com.github.pengpan.common.retry;

/**
 * @author pengpan
 */
public enum RetryRule {

    /**
     * 响应状态码不是2xx
     */
    RESPONSE_STATUS_NOT_2XX,

    /**
     * 发生任意异常
     */
    OCCUR_EXCEPTION,

    /**
     * 发生IO异常
     */
    OCCUR_IO_EXCEPTION,
}
