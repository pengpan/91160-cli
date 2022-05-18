package com.github.pengpan.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author pengpan
 */
@Getter
@AllArgsConstructor
public enum DataTypeEnum {

    CITIES("conf/cities.json"),
    DAYS("conf/days.json"),
    WEEKS("conf/weeks.json");

    private final String path;

}
