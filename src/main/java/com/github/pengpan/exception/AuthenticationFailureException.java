package com.github.pengpan.exception;

import java.util.Objects;

public class AuthenticationFailureException extends IllegalArgumentException{

    public AuthenticationFailureException(String s) {
        super(s);
    }

    public static void tryFail(String errorMsg) {
        if (Objects.equals(errorMsg,"登录账号校验失败")) {
            throw new AuthenticationFailureException(errorMsg);
        }
    }
}
