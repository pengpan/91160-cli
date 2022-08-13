package com.github.pengpan.entity;

import lombok.Data;

/**
 * @author pengpan
 */
@Data
public class CheckUser {

    private String code;

    private int error_num;

    private String msg;
}
