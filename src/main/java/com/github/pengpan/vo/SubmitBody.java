package com.github.pengpan.vo;

import lombok.Data;

import java.util.List;

@Data
public class SubmitBody {

    private String userName;

    private String password;

    private String cityId;

    private String unitId;

    private String deptId;

    private String doctorId;

    private String memberId;

    private List<String> days;

    private List<String> weeks;
}
