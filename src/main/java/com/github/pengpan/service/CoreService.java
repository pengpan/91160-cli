package com.github.pengpan.service;

import com.github.pengpan.entity.BrushSchData;
import com.github.pengpan.entity.Config;
import com.github.pengpan.enums.DataTypeEnum;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author pengpan
 */
public interface CoreService {


    List<Map<String, String>> getData(DataTypeEnum dataType);

    List<Map<String, String>> getUnit(String cityId);

    List<Map<String, Object>> getLocalUnit(String cityId);

    List<Map<String, Object>> getFullUnit(String cityId);

    List<Map<String, String>> getDept(String unitId);

    List<Map<String, String>> getDoctor(String unitId, String deptId);

    BrushSchData dept(String unitId, String deptId, String brushStartDate);

    List<Map<String, String>> getMember();

    void brushTicketTask(Config config);

    Date serverDate();
}
