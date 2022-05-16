package com.github.pengpan.cmd;

import io.airlift.airline.Command;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author pengpan
 */
@Command(name = "version", description = "Show version information")
public class Version implements Runnable {

    private static final String VERSION_PLACEHOLDER = "${project.version}";

    private static final String UNREADABLE_VERSION = "unreadable";
    private static final String UNSET_VERSION = "unset";
    private static final String UNKNOWN_VERSION = "unknown";

    public static String readVersionFromResources() {
        Properties versionProperties = new Properties();
        try (InputStream is = Version.class.getResourceAsStream("/version.properties")) {
            versionProperties.load(is);
        } catch (IOException ex) {
            ex.printStackTrace();
            return UNREADABLE_VERSION;
        }

        String version = versionProperties.getProperty("version", UNKNOWN_VERSION).trim();
        if (VERSION_PLACEHOLDER.equals(version)) {
            return UNSET_VERSION;
        } else {
            return version;
        }
    }

    @Override
    public void run() {
        String version = readVersionFromResources();
        System.out.println(version);
    }

}
