package com.github.pengpan.service;

import java.awt.image.BufferedImage;

/**
 * @author pengpan
 */
public interface DdddOcrService {

    String ocr(BufferedImage captchaImage);

    boolean baseUrlCheck(String baseUrl);
}
