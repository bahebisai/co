package com.xiaomi.emm.features.http;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Url;

/**
 * Created by Administrator on 2017/9/15.
 */

public interface SmsBackupService {
    @POST("/userSmsStrategy/upload")
    public Call<ResponseBody> sendSmsInfo(@Body RequestBody body);

    @POST()
    Call<ResponseBody> sendSmsInfo(@Url String url, @Body RequestBody body);
}
