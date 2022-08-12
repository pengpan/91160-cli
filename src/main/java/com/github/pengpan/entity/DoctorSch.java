package com.github.pengpan.entity;

import lombok.Data;

import java.util.Map;

/**
 * @author pengpan
 */
@Data
public class DoctorSch {

    private int code;

    private Map<String, String> dates;

    private String next;

    private String now;

    private String prev;

    private Map<String, Object> sch;
}
