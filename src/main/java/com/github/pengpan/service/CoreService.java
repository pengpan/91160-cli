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


    List<Map<String, Object>> getData(DataTypeEnum dataType);

    List<Map<String, Object>> getUnit(String cityId);

    List<Map<String, Object>> getDept(String unitId);

    List<Map<String, Object>> getDoctor(String unitId, String deptId);

    BrushSchData dept(String unitId, String deptId, String brushStartDate);

    List<Map<String, Object>> getMember();

    void brushTicketTask(Config config);

    Date serverDate();
}
