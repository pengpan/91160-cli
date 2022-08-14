package com.github.pengpan.entity;

import lombok.Builder;
import lombok.Data;

/**
 * @author pengpan
 */
@Builder
@Data
public class Appointment {

    private String name;

    private String value;
}
