package com.github.pengpan.cmd;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.github.pengpan.common.store.AccountStore;
import com.github.pengpan.common.store.ConfigStore;
import com.github.pengpan.common.util.CommonUtil;
import com.github.pengpan.entity.Prop;
import com.github.pengpan.enums.ChoseObjEnum;
import com.github.pengpan.service.CoreService;
import com.github.pengpan.vo.ChoseObj;
import io.airlift.airline.Command;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * @author pengpan
 */
@Slf4j
@Command(name = "init", description = "初始化数据")
public class Init implements Runnable {

    private final Scanner in = new Scanner(System.in);

    private final CoreService coreService = SpringUtil.getBean(CoreService.class);

    @Override
    public void run() {
        login();
        choseObj(ChoseObjEnum.MEMBER);
        choseObj(ChoseObjEnum.CITY);
        choseObj(ChoseObjEnum.UNIT);
        choseObj(ChoseObjEnum.BIG_DEPT);
        choseObj(ChoseObjEnum.DEPT);
        choseObj(ChoseObjEnum.DOCTOR);
        choseObj(ChoseObjEnum.WEEK);
        choseObj(ChoseObjEnum.DAY);
        storeConfig();
        CommonUtil.normalExit("init success.");
    }

    private void login() {
        boolean loginSuccess;
        do {
            String userName = AccountStore.getUserName();
            while (StrUtil.isBlank(userName)) {
                System.out.print("请输入用户名: ");
                userName = in.nextLine();
            }

            String password = AccountStore.getPassword();
            while (StrUtil.isBlank(password)) {
                System.out.print("请输入密码: ");
                password = in.nextLine();
            }

            log.info("登录中，请稍等...");

            loginSuccess = coreService.login(userName, password);
            log.info(loginSuccess ? "登录成功" : "用户名或密码错误，请重新输入！");
        } while (!loginSuccess);
    }

    private void choseObj(ChoseObjEnum choseObjEnum) {
        log.info("");
        ChoseObj choseObj = choseObjEnum.getChoseObj();
        log.info(choseObj.getBanner());
        List<Object> ids = new ArrayList<>();
        List<Map<String, Object>> data = choseObj.getData().apply(coreService);
        for (int i = 0; i < data.size(); i++) {
            Map<String, Object> datum = data.get(i);
            String id = String.valueOf("index".equals(choseObj.getAttrId()) ? (i + 1) : datum.get(choseObj.getAttrId()));
            ids.add(id);
            String name = StrUtil.format("[{}]. {}", id, datum.get(choseObj.getAttrName()));
            log.info(name);
        }
        boolean choseSuccess;
        do {
            String id = null;
            while (StrUtil.isBlank(id)) {
                System.out.print(choseObj.getInputTips());
                id = in.nextLine();
            }
            choseSuccess = checkInput(ids, id);
            if (choseSuccess) {
                choseObj.getStore().accept(id);
            } else {
                log.info("输入有误，请重新输入！");
            }
        } while (!choseSuccess);
    }

    private boolean checkInput(List<Object> ids, String id) {
        if (CollUtil.isEmpty(ids) || StrUtil.isBlank(id)) {
            return false;
        }
        if (ids.contains(id)) {
            return true;
        }
        List<String> split = StrUtil.split(id, ',');
        for (String s : split) {
            if (!ids.contains(s)) {
                return false;
            }
        }
        return true;
    }

    private void storeConfig() {
        List<Prop> props = new ArrayList<>();
        props.add(new Prop("91160账号", "userName", AccountStore.getUserName()));
        props.add(new Prop("91160密码", "password", AccountStore.getPassword()));
        props.add(new Prop("就诊人编号", "memberId", ConfigStore.getMemberId()));
        props.add(new Prop("城市编号", "cityId", ConfigStore.getCityId()));
        props.add(new Prop("医院编号", "unitId", ConfigStore.getUnitId()));
        props.add(new Prop("大科室编号", "bigDeptId", ConfigStore.getBigDeptId()));
        props.add(new Prop("小科室编号", "deptId", ConfigStore.getDeptId()));
        props.add(new Prop("医生编号", "doctorId", ConfigStore.getDoctorId()));
        props.add(new Prop("需要周几的号[可多选，如(6,7)]", "weekId", ConfigStore.getWeekId()));
        props.add(new Prop("时间段编号[可多选，如(am,pm)]", "dayId", ConfigStore.getDayId()));
        props.add(new Prop("刷号休眠时间[单位:秒]", "sleepTime", "15"));
        // custom config
        props.add(new Prop("是否开启定时挂号[true/false]", "enableAppoint", "false"));
        props.add(new Prop("定时挂号时间[格式: 2022-06-01 15:00:00]", "appointTime", ""));
        props.add(new Prop("是否开启多线程挂号(仅在定时挂号开启时生效)[true/false]", "enableMultithreading", "false"));
        props.add(new Prop("是否开启代理[true/false]", "enableProxy", "false"));
        props.add(new Prop("获取代理URL(可参考https://github.com/jhao104/proxy_pool搭建代理池)[格式: http://127.0.0.1:5010/get]", "getProxyURL", ""));

        StringBuilder sb = new StringBuilder();
        for (Prop prop : props) {
            String line1 = String.format("# %s", prop.getNote());
            String line2 = String.format("%s=%s", prop.getKey(), prop.getValue());
            sb.append(line1).append(System.lineSeparator()).append(line2).append(System.lineSeparator());
        }

        File file = new File("config.properties");
        FileUtil.writeUtf8String(sb.toString(), file);
        log.info("The file config.properties has been generated.");
    }

}
