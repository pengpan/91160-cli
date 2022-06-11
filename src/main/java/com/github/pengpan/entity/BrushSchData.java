package com.github.pengpan.entity;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author pengpan
 */
@Data
public class BrushSchData {

    private List<Map<String, Object>> doc;

    private Map<String, Object> sch;
}
