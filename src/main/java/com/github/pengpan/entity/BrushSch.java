package com.github.pengpan.entity;

import lombok.Data;

/**
 * @author pengpan
 */
@Data
public class BrushSch {

    private Integer result_code;

    private String error_code;

    private String error_msg;

    private BrushSchData data;
}
