package com.github.pengpan.cmd;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.github.pengpan.common.constant.SystemConstant;
import com.github.pengpan.common.store.AccountStore;
import com.github.pengpan.common.store.CapRegStore;
import com.github.pengpan.common.store.ConfigStore;
import com.github.pengpan.common.store.DdddOcrStore;
import com.github.pengpan.entity.InitData;
import com.github.pengpan.entity.Prop;
import com.github.pengpan.enums.InitDataEnum;
import com.github.pengpan.enums.OcrPlatformEnum;
import com.github.pengpan.service.CaptchaService;
import com.github.pengpan.service.CoreService;
import com.github.pengpan.service.DdddOcrService;
import com.github.pengpan.service.LoginService;
import com.github.pengpan.util.CommonUtil;
import io.airlift.airline.Command;
import io.airlift.airline.Option;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author pengpan
 */
@Slf4j
@Command(name = "init", description = "Initialization data")
public class Init implements Runnable {

    @Option(
            name = {"-c", "--config"},
            title = "configuration file",
            required = false,
            description = "Path to properties configuration file.")
    private String configFile;

    private final Scanner in = new Scanner(System.in);

    private final CoreService coreService = SpringUtil.getBean(CoreService.class);
    private final LoginService loginService = SpringUtil.getBean(LoginService.class);
    private final CaptchaService captchaService = SpringUtil.getBean(CaptchaService.class);
    private final DdddOcrService ddddOcrService = SpringUtil.getBean(DdddOcrService.class);

    @Override
    public void run() {
        tips();
        choseOcr();
        login();
        initData(InitDataEnum.MEMBER);
        initData(InitDataEnum.CITY);
        initData(InitDataEnum.UNIT);
        initData(InitDataEnum.DEPT);
        initData(InitDataEnum.DOCTOR);
        initData(InitDataEnum.WEEK);
        initData(InitDataEnum.DAY);
        initData(InitDataEnum.HOURS);
        initNotify();
        storeConfig();
        CommonUtil.normalExit("init success.");
    }

    private void tips() {
        List<String> tips = CollUtil.newArrayList();
        tips.add("脚本已接入斐斐打码用于识别图形验证码，请前往平台(http://www.fateadm.com/user_home.php)获取PD账号和PD秘钥");
        tips.add("[免费]脚本已接入ddddocr用于识别图形验证码，请前往91160-ocr-server(https://github.com/pengpan/91160-ocr-server)自行搭建服务，模型已经过训练，识别率很高");

        System.out.println();
        System.out.println("Tips: ");
        for (String tip : tips) {
            System.out.println("\t" + tip);
        }
        System.out.println();
    }

    private void choseOcr() {
        boolean check;
        do {
            String ocrPlatform = ConfigStore.getOcrPlatform();
            while (StrUtil.isBlank(ocrPlatform)) {
                System.out.print("请选择打码平台(1.斐斐打码/2.ddddocr),请输入1或2: ");
                ocrPlatform = in.nextLine();
            }
            List<String> ocrPlatformIds = Arrays.stream(OcrPlatformEnum.values())
                    .map(OcrPlatformEnum::getId)
                    .collect(Collectors.toList());
            check = ocrPlatformIds.contains(ocrPlatform);

            if (check) {
                ConfigStore.setOcrPlatform(ocrPlatform);
            }
        } while (!check);

        OcrPlatformEnum ocrPlatform = OcrPlatformEnum.getById(ConfigStore.getOcrPlatform());
        if (ocrPlatform == OcrPlatformEnum.FATEADM) {
            captcha();
        }
        if (ocrPlatform == OcrPlatformEnum.DDDDOCR) {
            ddddocr();
        }
    }

    private void captcha() {
        boolean captchaCheck;
        do {
            String pdId = CapRegStore.getPdId();
            while (StrUtil.isBlank(pdId)) {
                System.out.print("请输入斐斐打码PD账号: ");
                pdId = in.nextLine();
            }

            String pdKey = CapRegStore.getPdKey();
            while (StrUtil.isBlank(pdKey)) {
                System.out.print("请输入斐斐打码PD秘钥: ");
                pdKey = in.nextLine();
            }

            log.info("PD账号验证中，请稍等...");

            captchaCheck = captchaService.pdCheck(pdId, pdKey);

        } while (!captchaCheck);
    }

    private void ddddocr() {
        boolean captchaCheck;
        do {
            String baseUrl = DdddOcrStore.getBaseUrl();
            while (StrUtil.isBlank(baseUrl)) {
                System.out.print("请输入91160-ocr-server的服务地址[示例: http://127.0.0.1:8000 ]: ");
                baseUrl = in.nextLine();
            }

            log.info("服务地址验证中，请稍等...");

            captchaCheck = ddddOcrService.baseUrlCheck(baseUrl);

        } while (!captchaCheck);
    }

    private void initNotify() {
        System.out.print("请输入Server酱(https://sct.ftqq.com)的SendKey(用于挂号成功后通知，可跳过): ");
        String sendKey = in.nextLine();
        ConfigStore.setSendKey(sendKey);
    }

    private void login() {
        boolean loginSuccess;
        do {
            String userName = AccountStore.getUserName();
            while (StrUtil.isBlank(userName)) {
                System.out.print("请输入用户名（手机号码）: ");
                userName = in.nextLine();
            }

            String password = AccountStore.getPassword();
            while (StrUtil.isBlank(password)) {
                System.out.print("请输入密码: ");
                password = in.nextLine();
            }

            log.info("登录中，请稍等...");

            loginSuccess = loginService.doLoginRetry(userName, password, SystemConstant.MAX_LOGIN_RETRY);

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
        boolean allowEmpty = initDataEnum.getInitData().isAllowEmpty();
        do {
            String id = null;
            do {
                System.out.print(initData.getInputTips());
                id = in.nextLine();
            } while (StrUtil.isBlank(id) && !allowEmpty);

            log.info("input is: {}", id);
            success = checkInput(ids, id, allowEmpty);
            if (success) {
                initData.getStore().accept(id);
            } else {
                log.warn("输入有误，请重新输入！");
            }
        } while (!success);
    }

    private boolean checkInput(List<Object> ids, String id, boolean allowEmpty) {
        if (allowEmpty && StrUtil.isBlank(id)) {
            return true;
        }
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
        props.add(new Prop("打码平台", "ocrPlatform", ConfigStore.getOcrPlatform()));
        props.add(new Prop("91160-ocr-server的服务地址", "baseUrl", StrUtil.blankToDefault(DdddOcrStore.getBaseUrl(), "")));
        props.add(new Prop("斐斐打码PD账号", "pdId", StrUtil.blankToDefault(CapRegStore.getPdId(), "")));
        props.add(new Prop("斐斐打码PD秘钥", "pdKey", StrUtil.blankToDefault(CapRegStore.getPdKey(), "")));
        props.add(new Prop("91160账号", "userName", AccountStore.getUserName()));
        props.add(new Prop("91160密码", "password", AccountStore.getPassword()));
        props.add(new Prop("就诊人编号", "memberId", ConfigStore.getMemberId()));
        props.add(new Prop("城市编号", "cityId", ConfigStore.getCityId()));
        props.add(new Prop("医院编号", "unitId", ConfigStore.getUnitId()));
        props.add(new Prop("科室编号", "deptId", ConfigStore.getDeptId()));
        props.add(new Prop("医生编号[可多选，如(1001,1002)]", "doctorId", ConfigStore.getDoctorId()));
        props.add(new Prop("需要周几的号[可多选，如(6,7)]", "weeks", ConfigStore.getWeekId()));
        props.add(new Prop("时间段编号[可多选，如(am,pm)]", "days", ConfigStore.getDayId()));
        props.add(new Prop("时间点(如填写了多个且同时有号，提交先填写的。若为填写则不限制时间点)[可多选，如(10:00-10:30,08:00-08:30)]", "hours", ConfigStore.getHours()));
        props.add(new Prop("Server酱(https://sct.ftqq.com)的SendKey", "sendKey", ConfigStore.getSendKey()));
        props.add(new Prop("刷号休眠时间[单位:毫秒]", "sleepTime", "3000"));
        props.add(new Prop("刷号起始日期(表示刷该日期后一周的号,为空取当前日期)[格式: 2022-06-01]", "brushStartDate", ""));
        // custom config
        props.add(new Prop("是否开启定时挂号[true/false]", "enableAppoint", "false"));
        props.add(new Prop("定时挂号时间[格式: 2022-06-01 15:00:00]", "appointTime", ""));
        props.add(new Prop("是否开启代理[true/false]", "enableProxy", "false"));
        props.add(new Prop("代理文件路径[格式: /dir/proxy.txt]", "proxyFilePath", "proxy.txt"));
        props.add(new Prop("获取代理方式[ROUND_ROBIN(轮询)/RANDOM(随机)]", "proxyMode", "ROUND_ROBIN"));
        props.add(new Prop("刷号通道[CHANNEL_1(通道1)/CHANNEL_2(通道2)]", "brushChannel", ""));
        props.add(new Prop("就诊卡号", "medicalCard", ""));

        StringBuilder sb = new StringBuilder();
        for (Prop prop : props) {
            String line1 = String.format("# %s", prop.getNote());
            String line2 = String.format("%s=%s", prop.getKey(), prop.getValue());
            sb.append(line1).append(System.lineSeparator()).append(line2).append(System.lineSeparator());
        }

        File file = new File(StrUtil.blankToDefault(configFile, "config.properties"));
        FileUtil.writeUtf8String(sb.toString(), file);
        log.info("The file config.properties has been generated.");
    }

}
