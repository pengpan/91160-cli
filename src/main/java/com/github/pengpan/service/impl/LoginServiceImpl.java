package com.github.pengpan.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import com.github.pengpan.client.MainClient;
import com.github.pengpan.common.constant.SystemConstant;
import com.github.pengpan.common.store.AccountStore;
import com.github.pengpan.entity.CheckUser;
import com.github.pengpan.service.LoginService;
import com.github.pengpan.util.Assert;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

/**
 * @author pengpan
 */
@Slf4j
@Service
public class LoginServiceImpl implements LoginService {

    private static final RSA rsa = SecureUtil.rsa(null, SystemConstant.PUBLIC_KEY);

    @Resource
    private MainClient mainClient;

    @Override
    public String getToken() {
        String html = mainClient.htmlPage(SystemConstant.LOGIN_URL);
        Document document = Jsoup.parse(html);
        Element tokens = document.getElementById("tokens");
        Assert.notNull(tokens, "token获取失败");
        String token = tokens.val();
        log.info("token: " + token);
        return token;
    }

    @Override
    public boolean checkUser(String username, String password, String token) {
        String encryptedUsername = Base64.encode(rsa.encrypt(username, KeyType.PublicKey));
        String encryptedPassword = Base64.encode(rsa.encrypt(password, KeyType.PublicKey));

        Map<String, String> fields = MapUtil.newHashMap();
        fields.put("username", encryptedUsername);
        fields.put("password", encryptedPassword);
        fields.put("type", "m");
        fields.put("token", token);

        CheckUser checkUser = mainClient.checkUser(SystemConstant.CHECK_USER_URL, fields);
        Assert.notNull(checkUser, "用户检测失败");
        if (StrUtil.equals("1", checkUser.getCode())) {
            log.info("用户检测通过");
            return true;
        } else {
            log.warn("用户检测不通过: " + checkUser.getMsg());
            return false;
        }
    }

    @Override
    public boolean login(String username, String password, String token) {
        String encryptedUsername = Base64.encode(rsa.encrypt(username, KeyType.PublicKey));
        String encryptedPassword = Base64.encode(rsa.encrypt(password, KeyType.PublicKey));

        Map<String, String> fields = MapUtil.newHashMap();
        fields.put("username", encryptedUsername);
        fields.put("password", encryptedPassword);
        fields.put("target", SystemConstant.DOMAIN);
        fields.put("error_num", "0");
        fields.put("token", token);

        Response<Void> loginResp = mainClient.doLogin(SystemConstant.LOGIN_URL, fields);
        if (!loginResp.raw().isRedirect()) {
            log.warn("登录失败，请检查用户名和密码");
            return false;
        }

        String redirectUrl = loginResp.headers().get("Location");
        Response<String> redirectResp = mainClient.loginRedirect(redirectUrl);
        boolean loginSuccess = redirectResp.raw().isRedirect();
        if (loginSuccess) {
            AccountStore.store(username, password);
            log.info("登录成功");
        } else {
            log.warn("登录失败，请先去网页端(https://user.91160.com/login.html)登录成功后再次尝试");
            log.error("Response code: {}", redirectResp.code());
            String errorBody = null;
            try {
                if (redirectResp.errorBody() != null) {
                    errorBody = redirectResp.errorBody().string();
                }
            } catch (IOException ignored) {
            }
            log.error("Response errorBody: {}", errorBody);
        }
        return loginSuccess;
    }

    @Override
    public boolean doLogin(String username, String password) {
        String token = getToken();
        return checkUser(username, password, token)
                && login(username, password, token);
    }
}
