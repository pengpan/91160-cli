package com.github.pengpan.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author pengpan
 */
@Getter
@AllArgsConstructor
public enum LoginResultEnum {

    SUCCESS("SUCCESS", "登录成功"),
    FAILED("FAILED", "登录失败"),
    CAPTCHA_INCORRECT("CAPTCHA_INCORRECT", "验证码有误或已失效"),
    ;

    private final String code;

    private final String name;
}
