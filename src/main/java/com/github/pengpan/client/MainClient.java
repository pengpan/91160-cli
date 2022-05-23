package com.github.pengpan.client;

import com.alibaba.fastjson.JSONObject;
import retrofit2.Response;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;

/**
 * @author pengpan
 */
public interface MainClient {

    @GET
    String htmlPage(@Url String url);

    @FormUrlEncoded
    @POST
    Response<Void> doLogin(@Url String url, @FieldMap JSONObject fields);

    @GET
    Response<Void> loginRedirect(@Url String url);

    @FormUrlEncoded
    @POST("/ajax/getunitbycity.html")
    List<Map<String, Object>> getUnit(@Field("c") String cityId);

    @FormUrlEncoded
    @POST("/ajax/getdepbyunit.html")
    List<Map<String, Object>> getDept(@Field("keyValue") String unitId);

    @GET
    JSONObject dept(@Url String url,
                    @Query("unit_id") String unitId,
                    @Query("dep_id") String deptId,
                    @Query("date") String date,
                    @Query("p") int page,
                    @Query("user_key") String userKey);

    @FormUrlEncoded
    @POST("/doctors/ajaxgetclass.html")
    String brushTicket(@Field("docid") String docId,
                       @Field("date") String date,
                       @Field("days") int days);

    @GET("/guahao/ystep1/uid-{unitId}/depid-{deptId}/schid-{schId}.html")
    String orderPage(@Path("unitId") String unitId,
                     @Path("deptId") String deptId,
                     @Path("schId") String schId);

    @FormUrlEncoded
    @POST("/guahao/ysubmit.html")
    Response<Void> doSubmit(@Field("sch_data") String schData,
                            @Field("unit_id") String unitId,
                            @Field("dep_id") String depId,
                            @Field("doctor_id") String doctorId,
                            @Field("schedule_id") String schId,
                            @Field("mid") String memberId,
                            @Field("accept") String accept,
                            @Field("time_type") String time_type,
                            @Field("detlid") String detlid,
                            @Field("detlid_realtime") String detlid_realtime,
                            @Field("level_code") String level_code,
                            @Field("addressId") String addressId,
                            @Field("address") String address);
}
