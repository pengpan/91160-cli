package com.github.pengpan.entity;

import lombok.Data;

/**
 * @author pengpan
 */
@Data
public class ScheduleInfo {

    private String doctor_name;

    private int left_num;

    private String schedule_id;

    private String to_date;

    private String time_type;

}
