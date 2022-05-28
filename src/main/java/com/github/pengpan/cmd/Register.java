package com.github.pengpan.cmd;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.setting.dialect.Props;
import com.github.pengpan.entity.Config;
import com.github.pengpan.service.CoreService;
import com.github.pengpan.util.Assert;
import io.airlift.airline.Command;
import io.airlift.airline.Option;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author pengpan
 */
@Slf4j
@Command(name = "register", description = "Register on 91160.com")
public class Register implements Runnable {

    @Option(
            name = {"-c", "--config"},
            title = "configuration file",
            required = true,
            description = "Path to json configuration file.")
    private String configFile;

    @Override
    public void run() {
        Config config = getConfig(configFile);

        CoreService coreService = SpringUtil.getBean(CoreService.class);
        checkConfig(config, coreService);
        checkEnableAppoint(config, coreService);

        try {
            coreService.brushTicketTask(config);
            System.exit(0);
        } catch (Exception e) {
            log.error("", e);
            System.exit(-1);
        }
    }

    private void checkEnableAppoint(Config config, CoreService coreService) {
        if (config.isEnableAppoint()) {
            Date serverDate = coreService.serverDate();
            log.info("当前服务器时间: {}", DateUtil.formatDateTime(serverDate));

            Date appointTime = DateUtil.parse(config.getAppointTime()).toJdkDate();
            log.info("指定的挂号时间: {}", DateUtil.formatDateTime(appointTime));

            long waitTime = appointTime.getTime() - serverDate.getTime();
            waitTime = waitTime < 0 ? 0 : waitTime;
            log.info("需等待: {}秒", TimeUnit.MILLISECONDS.toSeconds(waitTime));

            log.info("等待中...");
            ThreadUtil.sleep(waitTime);
            log.info("时间到！！！");
        }
    }

    private Config getConfig(String configFile) {
        Assert.notBlank(configFile, "请指定配置文件");
        Assert.isTrue(configFile.endsWith(Props.EXT_NAME), "配置文件不正确");
        File file = FileUtil.file(configFile);
        Assert.isTrue(file.exists(), "配置文件不存在，请检查文件路径");
        Props props = new Props(file, CharsetUtil.CHARSET_UTF_8);
        Config config = new Config();
        props.fillBean(config, null);
        return config;
    }

    private void checkConfig(Config config, CoreService coreService) {
        // Required
        Assert.notBlank(config.getUserName(), "[userName]不能为空，请检查配置文件");
        Assert.notBlank(config.getPassword(), "[password]不能为空，请检查配置文件");
        Assert.isTrue(coreService.login(config.getUserName(), config.getPassword()), "登录失败，请检查账号和密码");
        Assert.notBlank(config.getMemberId(), "[memberId]不能为空，请检查配置文件");
        Assert.notBlank(config.getCityId(), "[cityId]不能为空，请检查配置文件");
        Assert.notBlank(config.getUnitId(), "[unitId]不能为空，请检查配置文件");
        Assert.notBlank(config.getDeptId(), "[deptId]不能为空，请检查配置文件");
        Assert.notBlank(config.getDoctorId(), "[doctorId]不能为空，请检查配置文件");
        Assert.isTrue(CollUtil.isNotEmpty(config.getWeeks()), "[weeks]不能为空，请检查配置文件");
        Assert.isTrue(CollUtil.isNotEmpty(config.getDays()), "[days]不能为空，请检查配置文件");
        Assert.isTrue(config.getSleepTime() >= 0, "[sleepTime]格式不正确，请检查配置文件");

        // Not required
        if (config.isEnableAppoint()) {
            Assert.notBlank(config.getAppointTime(), "[appointTime]不能为空");
            Assert.isTrue(isDateTime(config.getAppointTime()), "[appointTime]格式不正确，请检查配置文件");
        }
        if (config.isEnableProxy()) {
            Assert.notBlank(config.getGetProxyURL(), "[getProxyURL]不能为空");
            Assert.isTrue(Validator.isUrl(config.getGetProxyURL()), "[getProxyURL]格式不正确，请检查配置文件");
        }
    }

    private boolean isDateTime(String dateTime) {
        try {
            DateUtil.parse(dateTime, DatePattern.NORM_DATETIME_PATTERN);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}
