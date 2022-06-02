package com.github.pengpan.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author pengpan
 */
@Getter
@AllArgsConstructor
public enum ProxyModeEnum {

    /**
     * 轮询
     */
    ROUND_ROBIN,

    /**
     * 随机
     */
    RANDOM;
}