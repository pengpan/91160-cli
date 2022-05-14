package com.github.pengpan.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class ScheduleInfo {

    @JSONField(name = "left_num")
    private Integer number;

    @JSONField(name = "schedule_id")
    private String schId;

    @JSONField(name = "time_type")
    private String timeType;

}
