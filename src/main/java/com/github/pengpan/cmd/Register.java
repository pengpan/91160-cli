package com.github.pengpan.cmd;

import io.airlift.airline.Command;
import io.airlift.airline.Option;

@Command(name = "register", description = "挂号")
public class Register implements Runnable {

    @Option(
            name = {"-c", "--config"},
            title = "configuration file",
            description = "Path to json configuration file.")
    private String configFile;

    @Option(
            name = {"-u", "--username"},
            title = "用户名",
            description = "91160的账号")
    private String userName;

    @Option(
            name = {"-p", "--password"},
            title = "密码",
            description = "91160的密码")
    private String password;

    @Override
    public void run() {
        System.out.println("开始挂号");
    }
}
