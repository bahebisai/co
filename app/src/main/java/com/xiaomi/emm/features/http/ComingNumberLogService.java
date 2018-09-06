package com.xiaomi.emm.features.http;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by lenovo on 2017/9/22.
 */

public interface ComingNumberLogService {

    @POST("/log/postCallReminder")
    public Call<ResponseBody> sendComingNumberLog(@Body RequestBody body);
}
