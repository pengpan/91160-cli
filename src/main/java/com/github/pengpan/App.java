package com.github.pengpan;

import com.github.pengpan.cmd.Init;
import com.github.pengpan.cmd.Register;
import com.github.pengpan.cmd.Version;
import io.airlift.airline.Cli;
import io.airlift.airline.Help;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan
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

        Runnable runnable = builder.build().parse(args);

        try {
            new AnnotationConfigApplicationContext(App.class)
                    .getBean(runnable.getClass()).run();
        } catch (NoSuchBeanDefinitionException e) {
            runnable.run();
        }
    }
}
