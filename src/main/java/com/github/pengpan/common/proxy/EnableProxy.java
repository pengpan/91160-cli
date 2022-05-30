package com.github.pengpan.common.proxy;

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
public @interface EnableProxy {
}
