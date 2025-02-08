package com.github.pengpan.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.ejlchina.data.TypeRef;
import com.ejlchina.json.JSONKit;
import com.github.pengpan.client.FateadmClient;
import com.github.pengpan.common.store.CapRegStore;
import com.github.pengpan.entity.fateadm.CapJustResult;
import com.github.pengpan.entity.fateadm.CapRegResult;
import com.github.pengpan.entity.fateadm.CustValResult;
import com.github.pengpan.service.CaptchaService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author pengpan
 */
@Slf4j
@Service
public class CaptchaServiceImpl implements CaptchaService {

    @Resource
    private FateadmClient fateadmClient;

    @Override
    public CapRegResult capReg(BufferedImage captchaImage) {
        String timestamp = String.valueOf(DateUtil.currentSeconds());
        String sign = SecureUtil.md5(CapRegStore.getPdId() + timestamp + SecureUtil.md5(timestamp + CapRegStore.getPdKey()));

        Map<String, RequestBody> paramMap = new HashMap<>();
        paramMap.put("user_id", RequestBody.create(MediaType.parse("text/plain"), CapRegStore.getPdId()));
        paramMap.put("timestamp", RequestBody.create(MediaType.parse("text/plain"), timestamp));
        paramMap.put("sign", RequestBody.create(MediaType.parse("text/plain"), sign));
        paramMap.put("predict_type", RequestBody.create(MediaType.parse("text/plain"), "30400"));
        paramMap.put("up_type", RequestBody.create(MediaType.parse("text/plain"), "mt"));

        RequestBody image = RequestBody.create(MediaType.parse("multipart/form-data"), ImgUtil.toBytes(captchaImage, "png"));
        MultipartBody.Part imgData = MultipartBody.Part.createFormData("img_data", "image.png", image);

        String result = fateadmClient.capReg(paramMap, imgData);

        Map<String, String> resultMap = JSONKit.toBean(new TypeRef<Map<String, String>>() {
        }.getType(), result);
        if (!StrUtil.equals(resultMap.get("RetCode"), "0")) {
            log.warn("CapRegResult: " + result);

            if (StrUtil.equals(resultMap.get("RetCode"), "3002")) {
                log.error("请前往 斐斐打码(http://www.fateadm.com/user_home.php) 获取并配置 PD_ID(PD账号) 和 PD_KEY(PD秘钥) ");
            }

            return null;
        }

        Map<String, String> rspData = JSONKit.toBean(new TypeRef<Map<String, String>>() {
        }.getType(), resultMap.get("RspData"));

        CapRegResult capRegResult = new CapRegResult();
        capRegResult.setRetCode(resultMap.get("RetCode"));
        capRegResult.setErrMsg(resultMap.get("ErrMsg"));
        capRegResult.setRequestId(resultMap.get("RequestId"));
        capRegResult.setResult(rspData.get("result"));

        return capRegResult;
    }

    @Override
    public CapJustResult capJust(String requestId) {

        String timestamp = String.valueOf(DateUtil.currentSeconds());
        String sign = SecureUtil.md5(CapRegStore.getPdId() + timestamp + SecureUtil.md5(timestamp + CapRegStore.getPdKey()));

        String result = fateadmClient.capJust(CapRegStore.getPdId(), timestamp, sign, requestId);

        Map<String, String> resultMap = JSONKit.toBean(new TypeRef<Map<String, String>>() {
        }.getType(), result);
        if (!StrUtil.equals(resultMap.get("RetCode"), "0")) {
            log.warn("CapJustResult: " + result);
            return null;
        }

        CapJustResult capJustResult = new CapJustResult();
        capJustResult.setRetCode(resultMap.get("RetCode"));
        capJustResult.setErrMsg(resultMap.get("ErrMsg"));

        return capJustResult;
    }

    @Override
    public CustValResult custval(String pdId, String pdKey) {

        String timestamp = String.valueOf(DateUtil.currentSeconds());
        String sign = SecureUtil.md5(pdId + timestamp + SecureUtil.md5(timestamp + pdKey));

        String result = fateadmClient.custval(pdId, timestamp, sign);

        Map<String, String> resultMap = JSONKit.toBean(new TypeRef<Map<String, String>>() {
        }.getType(), result);
        if (!StrUtil.equals(resultMap.get("RetCode"), "0")) {
            log.warn("CustValResult: " + result);
            return null;
        }

        Map<String, String> rspData = JSONKit.toBean(new TypeRef<Map<String, String>>() {
        }.getType(), resultMap.get("RspData"));

        CustValResult custValResult = new CustValResult();
        custValResult.setRetCode(resultMap.get("RetCode"));
        custValResult.setErrMsg(resultMap.get("ErrMsg"));
        custValResult.setCustVal(rspData.get("cust_val"));

        return custValResult;
    }

    @Override
    public boolean pdCheck(String pdId, String pdKey) {
        CustValResult custval = custval(pdId, pdKey);
        if (custval == null) {
            log.warn("验证失败，请检查PD账号和PD秘钥是否正确");
            return false;
        }

        BigDecimal custVal = Optional.ofNullable(custval.getCustVal())
                .map(BigDecimal::new)
                .orElse(BigDecimal.ZERO);
        log.info("custVal: {}", custVal);

        if (custVal.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("验证失败，积分余额不足，请充值！");
            return false;
        }

        CapRegStore.store(pdId, pdKey);
        log.info("验证成功");

        return true;
    }
}
