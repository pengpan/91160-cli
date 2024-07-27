package com.github.pengpan.cmd;

import com.github.pengpan.common.cookie.CookieStore;
import io.airlift.airline.Cli;
import io.airlift.airline.Command;
import io.airlift.airline.Help;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * @author pengpan
 */
@Slf4j
@Command(name = "startup", description = "Startup on 91160.com")
public class Startup implements Runnable {

    @Override
    public void run() {
        System.setProperty("isExit", "false");

        Cli<Runnable> builder = Cli.<Runnable>builder("91160-cli")
                .withDefaultCommand(Help.class)
                .withCommands(Init.class, Register.class).build();

        File file = new File("config.properties");
        if (!file.exists()) {
            builder.parse("init").run();
        }

        CookieStore.clear();

        builder.parse("register", "-c", "config.properties").run();
    }
}
