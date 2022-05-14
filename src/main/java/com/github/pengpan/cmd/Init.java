package com.github.pengpan.cmd;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pengpan.common.store.AccountStore;
import com.github.pengpan.common.store.ConfigStore;
import com.github.pengpan.enums.ChoseObjEnum;
import com.github.pengpan.service.CoreService;
import com.github.pengpan.vo.ChoseObj;
import io.airlift.airline.Command;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

@Command(name = "init", description = "初始化数据")
public class Init implements Runnable {

    private final CoreService coreService = new CoreService();

    private final Scanner in = new Scanner(System.in);

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
        System.out.println("init success.");
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

            System.out.println("登录中，请稍等...");

            loginSuccess = coreService.login(userName, password);
            if (loginSuccess) {
                AccountStore.store(userName, password);
                System.out.println("登录成功");
            } else {
                System.out.println("用户名或密码错误，请重新输入！");
            }
        } while (!loginSuccess);
    }

    private void choseObj(ChoseObjEnum choseObjEnum) {
        System.out.println();
        ChoseObj choseObj = choseObjEnum.getChoseObj();
        System.out.println(choseObj.getBanner());
        List<Object> ids = new ArrayList<>();
        List<Map<String, Object>> data = choseObj.getData().get();
        for (int i = 0; i < data.size(); i++) {
            Map<String, Object> datum = data.get(i);
            String id = String.valueOf("index".equals(choseObj.getAttrId()) ? (i + 1) : datum.get(choseObj.getAttrId()));
            ids.add(id);
            String name = StrUtil.format("{}. {}", id, datum.get(choseObj.getAttrName()));
            System.out.println(name);
        }
        boolean choseSuccess;
        do {
            String id = choseObj.getGetValue().get();
            while (StrUtil.isBlank(id)) {
                System.out.print(choseObj.getInputTips());
                id = in.nextLine();
            }
            choseSuccess = checkInput(ids, id);
            if (choseSuccess) {
                choseObj.getSetValue().accept(id);
            } else {
                System.out.println("输入有误，请重新输入！");
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
        System.out.println("The file config.json has been generated.");
    }

}
