package com.github.pengpan.cmd;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.github.pengpan.common.store.AccountStore;
import com.github.pengpan.common.store.ConfigStore;
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
        log.info("init success.");
        System.exit(0);
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
            if (loginSuccess) {
                AccountStore.store(userName, password);
                log.info("登录成功");
            } else {
                log.info("用户名或密码错误，请重新输入！");
            }
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
        String config = ConfigStore.toJson();
        File file = new File("config.json");
        FileUtil.writeUtf8String(config, file);
        log.info("The file config.json has been generated.");
    }

}
