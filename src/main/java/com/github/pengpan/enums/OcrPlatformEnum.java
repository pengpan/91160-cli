package com.github.pengpan.enums;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author pengpan
 */
@Getter
@AllArgsConstructor
public enum OcrPlatformEnum {

    FATEADM("1", "FATEADM", "斐斐打码"),
    DDDDOCR("2", "DDDDOCR", "ddddocr"),
    ;

    private final String id;

    private final String code;

    private final String name;

    public static OcrPlatformEnum getById(String id) {
        for (OcrPlatformEnum value : OcrPlatformEnum.values()) {
            if (StrUtil.equals(value.getId(), id)) {
                return value;
            }
        }
        return null;
    }
}
