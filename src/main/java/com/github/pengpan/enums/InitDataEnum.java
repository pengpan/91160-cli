package com.github.pengpan.enums;

import com.github.pengpan.common.store.ConfigStore;
import com.github.pengpan.entity.InitData;
import com.github.pengpan.service.CoreService;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author pengpan
 */
@Getter
@AllArgsConstructor
public enum InitDataEnum {

    MEMBER(InitData.builder()
            .attrId("id")
            .attrName("name")
            .banner("=====请选择就诊人=====")
            .inputTips("请输入就诊人编号（中括号中间的值为编号，下同）: ")
            .data(CoreService::getMember)
            .store(ConfigStore::setMemberId)
            .build()),

    CITY(InitData.builder()
            .attrId("cityId")
            .attrName("name")
            .banner("=====请选择城市=====")
            .inputTips("请输入城市编号: ")
            .data(x -> x.getData(DataTypeEnum.CITIES))
            .store(ConfigStore::setCityId)
            .build()),

    UNIT(InitData.builder()
            .attrId("unit_id")
            .attrName("unit_name")
            .banner("=====请选择医院=====")
            .inputTips("请输入医院编号: ")
            .data(x -> x.getUnit(ConfigStore.getCityId()))
            .store(ConfigStore::setUnitId)
            .build()),

    DEPT(InitData.builder()
            .attrId("dep_id")
            .attrName("dep_name")
            .banner("=====请选择科室=====")
            .inputTips("请输入科室编号: ")
            .data(x -> x.getDept(ConfigStore.getUnitId()))
            .store(ConfigStore::setDeptId)
            .build()),

    DOCTOR(InitData.builder()
            .attrId("doctor_id")
            .attrName("doctor_name")
            .banner("=====请选择医生=====")
            .inputTips("请输入医生编号: ")
            .data(x -> x.getDoctor(ConfigStore.getUnitId(), ConfigStore.getDeptId()))
            .store(ConfigStore::setDoctorId)
            .build()),

    WEEK(InitData.builder()
            .attrId("value")
            .attrName("name")
            .banner("=====请选择哪天的号=====")
            .inputTips("请输入需要周几的号[可多选，如(6,7)]: ")
            .data(x -> x.getData(DataTypeEnum.WEEKS))
            .store(ConfigStore::setWeekId)
            .build()),

    DAY(InitData.builder()
            .attrId("value")
            .attrName("name")
            .banner("=====请选择时间段=====")
            .inputTips("请输入时间段编号[可多选，如(am,pm)]: ")
            .data(x -> x.getData(DataTypeEnum.DAYS))
            .store(ConfigStore::setDayId)
            .build()),
    ;

    private InitData initData;
}
