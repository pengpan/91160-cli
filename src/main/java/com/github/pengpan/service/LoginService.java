package com.github.pengpan.service;

/**
 * @author pengpan
 */
public interface LoginService {

    String getToken();

    boolean checkUser(String username, String password, String token);

    boolean login(String username, String password, String token);

    boolean doLogin(String username, String password);
}
