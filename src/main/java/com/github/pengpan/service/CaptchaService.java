package com.github.pengpan.service;

import com.github.pengpan.entity.fateadm.CapJustResult;
import com.github.pengpan.entity.fateadm.CapRegResult;
import com.github.pengpan.entity.fateadm.CustValResult;

import java.awt.image.BufferedImage;

/**
 * @author pengpan
 */
public interface CaptchaService {

    CapRegResult capReg(BufferedImage captchaImage);

    CapJustResult capJust(String requestId);

    CustValResult custval(String pdId, String pdKey);

    boolean pdCheck(String pdId, String pdKey);
}
