package com.github.pengpan.client;

import com.github.pengpan.entity.ddddocr.OcrResult;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Url;

/**
 * @author pengpan
 */
public interface DdddOcrClient {

    @FormUrlEncoded
    @POST("/ocr")
    OcrResult ocr(@Field("image") String image);

    @FormUrlEncoded
    @POST
    OcrResult ocr(@Url String url, @Field("image") String image);
}
