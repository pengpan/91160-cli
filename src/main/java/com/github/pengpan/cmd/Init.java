package com.github.pengpan.cmd;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.github.pengpan.common.store.AccountStore;
import com.github.pengpan.common.store.ConfigStore;
import com.github.pengpan.entity.InitData;
import com.github.pengpan.entity.Prop;
import com.github.pengpan.enums.InitDataEnum;
import com.github.pengpan.service.CoreService;
import com.github.pengpan.service.LoginService;
import com.github.pengpan.util.CommonUtil;
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
@Command(name = "init", description = "Initialization data")
public class Init implements Runnable {

    private final Scanner in = new Scanner(System.in);

    private final CoreService coreService = SpringUtil.getBean(CoreService.class);
    private final LoginService loginService = SpringUtil.getBean(LoginService.class);

    @Override
    public void run() {
        login();
        initData(InitDataEnum.MEMBER);
        initData(InitDataEnum.CITY);
        initData(InitDataEnum.UNIT);
        initData(InitDataEnum.DEPT);
        initData(InitDataEnum.DOCTOR);
        initData(InitDataEnum.WEEK);
        initData(InitDataEnum.DAY);
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

            loginSuccess = loginService.doLogin(userName, password);

        } while (!loginSuccess);
    }

    private void initData(InitDataEnum initDataEnum) {
        log.info("");
        InitData initData = initDataEnum.getInitData();
        log.info(initData.getBanner());
        List<Object> ids = new ArrayList<>();
        List<Map<String, Object>> data = initData.getData().apply(coreService);
        for (Map<String, Object> datum : data) {
            String id = String.valueOf(datum.get(initData.getAttrId()));
            ids.add(id);
            String name = StrUtil.format("[{}]. {}", id, datum.get(initData.getAttrName()));
            log.info(name);
        }
        boolean success;
        do {
            String id = null;
            while (StrUtil.isBlank(id)) {
                System.out.print(initData.getInputTips());
                id = in.nextLine();
            }
            success = checkInput(ids, id);
            if (success) {
                initData.getStore().accept(id);
            } else {
                log.warn("输入有误，请重新输入！");
            }
        } while (!success);
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
        props.add(new Prop("科室编号", "deptId", ConfigStore.getDeptId()));
        props.add(new Prop("医生编号", "doctorId", ConfigStore.getDoctorId()));
        props.add(new Prop("需要周几的号[可多选，如(6,7)]", "weeks", ConfigStore.getWeekId()));
        props.add(new Prop("时间段编号[可多选，如(am,pm)]", "days", ConfigStore.getDayId()));
        props.add(new Prop("刷号休眠时间[单位:毫秒]", "sleepTime", "5000"));
        props.add(new Prop("刷号起始日期(表示刷该日期后一周的号,为空取当前日期)[格式: 2022-06-01]", "brushStartDate", ""));
        // custom config
        props.add(new Prop("是否开启定时挂号[true/false]", "enableAppoint", "false"));
        props.add(new Prop("定时挂号时间[格式: 2022-06-01 15:00:00]", "appointTime", ""));
        props.add(new Prop("是否开启代理[true/false]", "enableProxy", "false"));
        props.add(new Prop("代理文件路径[格式: /dir/proxy.txt]", "proxyFilePath", "proxy.txt"));
        props.add(new Prop("获取代理方式[ROUND_ROBIN(轮询)/RANDOM(随机)]", "proxyMode", "ROUND_ROBIN"));
        props.add(new Prop("刷号通道[CHANNEL_1(通道1)/CHANNEL_2(通道2)]", "brushChannel", ""));

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
