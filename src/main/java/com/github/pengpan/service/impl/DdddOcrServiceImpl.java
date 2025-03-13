package com.github.pengpan.service.impl;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.github.pengpan.client.DdddOcrClient;
import com.github.pengpan.common.store.DdddOcrStore;
import com.github.pengpan.entity.ddddocr.OcrResult;
import com.github.pengpan.service.DdddOcrService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.awt.image.BufferedImage;
import java.util.Optional;

/**
 * @author pengpan
 */
@Slf4j
@Service
public class DdddOcrServiceImpl implements DdddOcrService {

    @Resource
    private DdddOcrClient ddddOcrClient;

    @Override
    public String ocr(BufferedImage captchaImage) {
        String image = ImgUtil.toBase64(captchaImage, "png");

        String baseUrl = DdddOcrStore.getBaseUrl();
        OcrResult ocrResult = StrUtil.isBlank(baseUrl)
                ? ddddOcrClient.ocr(image)
                : ddddOcrClient.ocr(baseUrl + "/ocr", image);

        if (ocrResult != null && ocrResult.isOk()) {
            return ocrResult.getData();
        }
        log.error("ocr error, msg: {}", Optional.ofNullable(ocrResult).map(OcrResult::getMsg).orElse(null));
        return null;
    }

    @Override
    public boolean baseUrlCheck(String baseUrl) {
        boolean checkResult;
        try {
            String result = HttpUtil.get(baseUrl, 3000);
            checkResult = result != null && result.contains("Not Found");
        } catch (Exception e) {
            checkResult = false;
        }

        if (!checkResult) {
            log.warn("验证失败，请检查服务地址是否正确");
            return false;
        }

        DdddOcrStore.store(baseUrl);
        log.info("验证成功");

        return true;
    }
}
