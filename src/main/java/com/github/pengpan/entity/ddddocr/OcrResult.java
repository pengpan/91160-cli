package com.github.pengpan.entity.ddddocr;

import lombok.Data;

/**
 * @author pengpan
 */
@Data
public class OcrResult {

    private String code;

    private String msg;

    private String data;

    public boolean isOk() {
        return "200".equals(this.code) && "ok".equals(this.msg);
    }
}
