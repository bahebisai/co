package com.xiaomi.emm.features.http;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by Administrator on 2017/9/12.
 */

public interface SwitchLogService {
    @POST("/log/postSystemSwitch")
    public Call<ResponseBody> sendSwitchLog(@Body RequestBody body);
}
