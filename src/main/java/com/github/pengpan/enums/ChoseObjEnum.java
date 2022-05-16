package com.github.pengpan.enums;

import cn.hutool.core.lang.TypeReference;
import com.alibaba.fastjson.JSON;
import com.github.pengpan.common.store.ConfigStore;
import com.github.pengpan.service.CoreService;
import com.github.pengpan.vo.ChoseObj;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum ChoseObjEnum {

    MEMBER(ChoseObj.builder()
            .attrId("id")
            .attrName("name")
            .banner("=====请选择就诊人=====")
            .inputTips("请输入就诊人编号: ")
            .data(CoreService::getMember)
            .getValue(ConfigStore::getMemberId)
            .setValue(ConfigStore::setMemberId)
            .build()),

    CITY(ChoseObj.builder()
            .attrId("cityId")
            .attrName("name")
            .banner("=====请选择城市=====")
            .inputTips("请输入城市编号: ")
            .data(x -> x.getData(DataTypeEnum.CITIES))
            .getValue(ConfigStore::getCityId)
            .setValue(ConfigStore::setCityId)
            .build()),

    UNIT(ChoseObj.builder()
            .attrId("unit_id")
            .attrName("unit_name")
            .banner("=====请选择医院=====")
            .inputTips("请输入医院编号: ")
            .data(x -> x.getUnit(ConfigStore.getCityId()))
            .getValue(ConfigStore::getUnitId)
            .setValue(ConfigStore::setUnitId)
            .build()),

    BIG_DEPT(ChoseObj.builder()
            .attrId("index")
            .attrName("pubcat")
            .banner("=====请选择大科室=====")
            .inputTips("请输入大科室编号: ")
            .data(x -> x.getDept(ConfigStore.getUnitId()))
            .getValue(ConfigStore::getBigDeptId)
            .setValue(ConfigStore::setBigDeptId)
            .build()),

    DEPT(ChoseObj.builder()
            .attrId("dep_id")
            .attrName("dep_name")
            .banner("=====请选择小科室=====")
            .inputTips("请输入小科室编号: ")
            .data(x -> {
                List<Map<String, Object>> dept = x.getDept(ConfigStore.getUnitId());
                Map<String, Object> bigDept = dept.get(Integer.parseInt(ConfigStore.getBigDeptId()) - 1);
                String child = Optional.ofNullable(bigDept.get("childs")).map(JSON::toJSONString).orElseGet(String::new);
                return JSON.parseArray(child).stream()
                        .map(JSON::toJSONString)
                        .map(y -> JSON.<Map<String, Object>>parseObject(y, new TypeReference<LinkedHashMap<String, Object>>() {
                        }.getType()))
                        .collect(Collectors.toList());
            })
            .getValue(ConfigStore::getDeptId)
            .setValue(ConfigStore::setDeptId)
            .build()),

    DOCTOR(ChoseObj.builder()
            .attrId("doctor_id")
            .attrName("doctor_name")
            .banner("=====请选择医生=====")
            .inputTips("请输入医生编号: ")
            .data(x -> x.getDoctor(ConfigStore.getUnitId(), ConfigStore.getDeptId()))
            .getValue(ConfigStore::getDoctorId)
            .setValue(ConfigStore::setDoctorId)
            .build()),

    WEEK(ChoseObj.builder()
            .attrId("value")
            .attrName("name")
            .banner("=====请选择哪天的号=====")
            .inputTips("请输入需要周几的号[可多选，如(6,7)]: ")
            .data(x -> x.getData(DataTypeEnum.WEEKS))
            .getValue(ConfigStore::getWeekId)
            .setValue(ConfigStore::setWeekId)
            .build()),

    DAY(ChoseObj.builder()
            .attrId("value")
            .attrName("name")
            .banner("=====请选择时间段=====")
            .inputTips("请输入时间段编号[可多选，如(am,pm)]: ")
            .data(x -> x.getData(DataTypeEnum.DAYS))
            .getValue(ConfigStore::getDayId)
            .setValue(ConfigStore::setDayId)
            .build()),
    ;

    private ChoseObj choseObj;
}
