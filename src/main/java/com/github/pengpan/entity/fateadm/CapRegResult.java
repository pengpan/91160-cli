package com.github.pengpan.entity.fateadm;

import lombok.Data;

/**
 * @author pengpan
 */
@Data
public class CapRegResult {

    private String retCode;

    private String errMsg;

    private String requestId;

    private String result;
}
