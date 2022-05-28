package com.github.pengpan.entity;

import com.github.pengpan.service.CoreService;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author pengpan
 */
@Data
@Builder
public class InitData {

    private String attrId;

    private String attrName;

    private String banner;

    private String inputTips;

    private Function<CoreService, List<Map<String, Object>>> data;

    private Consumer<String> store;
}
