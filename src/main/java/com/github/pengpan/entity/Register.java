package com.github.pengpan.entity;

import lombok.Builder;
import lombok.Data;

/**
 * @author pengpan
 */
@Data
@Builder
public class Register {

    private String schData;
    private String unitId;
    private String depId;
    private String doctorId;
    private String schId;
    private String memberId;
    private String accept;
    private String timeType;
    private String detlName;
    private String detlid;
    private String detlidRealtime;
    private String levelCode;
    private String addressId;
    private String address;
    private String toDate;
}
