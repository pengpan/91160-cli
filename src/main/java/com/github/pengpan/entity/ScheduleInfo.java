package com.github.pengpan.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author pengpan
 */
@Data
public class ScheduleInfo {

    @JSONField(name = "left_num")
    private Integer number;

    @JSONField(name = "schedule_id")
    private String schId;

    @JSONField(name = "time_type")
    private String timeType;

}
