package com.github.pengpan;

import com.github.pengpan.cmd.Init;
import com.github.pengpan.cmd.Register;
import com.github.pengpan.cmd.Version;
import io.airlift.airline.Cli;
import io.airlift.airline.Help;

public class App {

    public static void main(String[] args) {
        String version = Version.readVersionFromResources();

        Cli.CliBuilder<Runnable> builder =
                Cli.<Runnable>builder("91160-cli")
                        .withDescription(String.format("91160 CLI (version %s).", version))
                        .withDefaultCommand(Help.class)
                        .withCommands(Help.class,
                                Version.class,
                                Init.class,
                                Register.class
                        );
        builder.build().parse(args).run();
    }
}
