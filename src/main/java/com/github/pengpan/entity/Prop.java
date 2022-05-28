package com.github.pengpan.entity;

import lombok.Data;

/**
 * @author pengpan
 */
@Data
public class Prop {

    private String note;

    private String key;

    private String value;

    public Prop() {
    }

    public Prop(String note, String key, String value) {
        this.note = note;
        this.key = key;
        this.value = value;
    }
}
