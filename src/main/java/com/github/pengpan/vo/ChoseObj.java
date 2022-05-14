package com.github.pengpan.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Data
@Builder
public class ChoseObj {

    private String attrId;

    private String attrName;

    private String banner;

    private String inputTips;

    private Supplier<List<Map<String, Object>>> data;

    private Supplier<String> getValue;

    private Consumer<String> setValue;
}
