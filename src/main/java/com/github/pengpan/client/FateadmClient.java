package com.github.pengpan.client;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.*;

import java.util.Map;

/**
 * @author pengpan
 */
public interface FateadmClient {

    @Multipart
    @POST("/api/capreg")
    String capReg(@PartMap Map<String, RequestBody> paramMap,
                  @Part MultipartBody.Part imgData);

    @FormUrlEncoded
    @POST("/api/capjust")
    String capJust(@Field("user_id") String userId,
                   @Field("timestamp") String timestamp,
                   @Field("sign") String sign,
                   @Field("request_id") String requestId);

    @FormUrlEncoded
    @POST("/api/custval")
    String custval(@Field("user_id") String userId,
                   @Field("timestamp") String timestamp,
                   @Field("sign") String sign);
}
