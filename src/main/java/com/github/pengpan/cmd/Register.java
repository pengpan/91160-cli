package com.github.pengpan.cmd;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pengpan.service.CoreService;
import com.github.pengpan.vo.SubmitBody;
import io.airlift.airline.Command;
import io.airlift.airline.Option;
import lombok.extern.slf4j.Slf4j;

/**
 * @author pengpan
 */
@Slf4j
@Command(name = "register", description = "挂号")
public class Register implements Runnable {

    @Option(
            name = {"-c", "--config"},
            title = "configuration file",
            required = true,
            description = "Path to json configuration file.")
    private String configFile;

    @Option(
            name = {"-u", "--username"},
            title = "用户名",
            required = true,
            description = "91160的账号")
    private String userName;

    @Option(
            name = {"-p", "--password"},
            title = "密码",
            required = true,
            description = "91160的密码")
    private String password;

    @Option(
            name = {"-s", "--sleep-time"},
            title = "休眠时间，单位秒",
            description = "刷号休眠时间，默认10秒")
    private Integer sleepTime;

    @Override
    public void run() {
        CoreService coreService = SpringUtil.getBean(CoreService.class);
        if (sleepTime == null) {
            sleepTime = 10;
        } else if (sleepTime < 0) {
            log.info("休眠时间不能小于0");
        } else if (sleepTime < 5) {
            log.info("不建议休眠时间小于5秒，容易触发限制访问");
        }

        if (!FileUtil.exist(configFile)) {
            log.info("配置文件不存在，请检查文件路径");
            System.exit(-1);
        }

        String content = FileUtil.readUtf8String(configFile);
        if (StrUtil.isBlank(content)) {
            log.info("配置文件不能为空");
            System.exit(-1);
        }

        if (!JSONUtil.isTypeJSONObject(content)) {
            log.info("配置文件内容不正确");
            System.exit(-1);
        }

        JSONObject config = JSON.parseObject(content);

        String memberId = config.getString("memberId");
        String cityId = config.getString("cityId");
        String unitId = config.getString("unitId");
        String bigDeptId = config.getString("bigDeptId");
        String deptId = config.getString("deptId");
        String doctorId = config.getString("doctorId");
        String weekId = config.getString("weekId");
        String dayId = config.getString("dayId");
        if (StrUtil.hasBlank(memberId, cityId, unitId, bigDeptId, deptId, doctorId, weekId, dayId)) {
            log.info("配置文件存在为空的属性");
            System.exit(-1);
        }

        boolean login = coreService.login(userName, password);
        if (!login) {
            log.info("登录失败，请检查用户名密码");
            System.exit(-1);
        }

        SubmitBody s = new SubmitBody();
        s.setUserName(userName);
        s.setPassword(password);
        s.setCityId(cityId);
        s.setUnitId(unitId);
        s.setDeptId(deptId);
        s.setDoctorId(doctorId);
        s.setMemberId(memberId);
        s.setWeeks(JSON.parseArray(weekId, String.class));
        s.setDays(JSON.parseArray(dayId, String.class));

        try {
            coreService.brushTicketTask(s, sleepTime);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
