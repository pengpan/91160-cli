package com.github.pengpan.cmd;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pengpan.service.CoreService;
import com.github.pengpan.vo.SubmitBody;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

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

    @Override
    public void run() {
        CoreService coreService = new CoreService();

        if (!FileUtil.exist(configFile)) {
            System.out.println("配置文件不存在，请检查文件路径");
            System.exit(-1);
        }

        String content = FileUtil.readUtf8String(configFile);
        if (StrUtil.isBlank(content)) {
            System.out.println("配置文件不能为空");
            System.exit(-1);
        }

        if (!JSONUtil.isTypeJSONObject(content)) {
            System.out.println("配置文件内容不正确");
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
            System.out.println("配置文件存在为空的属性");
            System.exit(-1);
        }

        boolean login = coreService.login(userName, password);
        if (!login) {
            System.out.println("登录失败，请检查用户名密码");
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
        s.setWeeks(StrUtil.split(weekId, ","));
        s.setDays(StrUtil.split(dayId, ","));

        try {
            coreService.brushTicketTask(s);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

    }
}
