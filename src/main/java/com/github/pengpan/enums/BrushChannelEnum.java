package com.github.pengpan.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author pengpan
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum BrushChannelEnum {

    /**
     * 通道1
     */
    CHANNEL_1("通道1"),

    /**
     * 通道2
     */
    CHANNEL_2("通道2");

    private String name;
}
