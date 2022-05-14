package com.github.pengpan.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterForm {

    private String schData;
    private String unitId;
    private String depId;
    private String doctorId;
    private String schId;
    private String memberId;
    private String accept;
    private String timeType;
    private String detlid;
    private String detlidRealtime;
    private String levelCode;
    private String addressId;
    private String address;

}
