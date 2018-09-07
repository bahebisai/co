package com.xiaomi.emm.features.http;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by Administrator on 2017/9/15.
 */

public interface RequestService {
    @POST()
    Call<ResponseBody> uploadInfo(@Url String url, @Body RequestBody body);

    @Multipart
    @POST()
    public Call<ResponseBody> uploadInfo(@Url String url, @Part("description") RequestBody description, @Part MultipartBody.Part file);

    @Multipart
    @POST()
    public Call<ResponseBody> uploadPhoto(@Url String url, @Part("alias") RequestBody description, @Part MultipartBody.Part file);//todo baii same as above, 跟后台协商修改


    @GET()
    Call<ResponseBody> getInfo(@Url String url);

    @GET()
    Call<ResponseBody> getInfo(@Url String url, @QueryMap Map<String, String> map);


    @Streaming
    @GET
    Call<ResponseBody> getStreamInfo(@Url String fileUr);//l//TODO BAII

    @Streaming
    @GET("/web/apps/download")
    Call<ResponseBody> downloadFile(@Header("Range") String range, @Query("alias") String alias, @Query("id") String id, @Query("code") String type);
}
