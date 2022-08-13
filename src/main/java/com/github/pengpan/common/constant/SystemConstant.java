package com.github.pengpan.common.constant;

import java.util.regex.Pattern;

/**
 * @author pengpan
 */
public class SystemConstant {

    public final static String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDWuY4Gff8FO3BAKetyvNgGrdZM9CMNoe45SzHMXxAPWw6E2idaEjqe5uJFjVx55JW+5LUSGO1H5MdTcgGEfh62ink/cNjRGJpR25iVDImJlLi2izNs9zrQukncnpj6NGjZu/2z7XXfJb4XBwlrmR823hpCumSD1WiMl1FMfbVorQIDAQAB";

    public final static String DEFECT_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.75 Safari/537.36";

    public final static String DOMAIN = "https://www.91160.com";

    public final static String LOGIN_URL = "https://user.91160.com/login.html";

    public final static String CHECK_USER_URL = "https://user.91160.com/checkUser.html";

    public final static Pattern PROXY_PATTERN = Pattern.compile("(socks|http)@(.*):(.*)");

    public final static int LIMIT_RETRIES = 100;
}
