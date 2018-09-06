package com.xiaomi.emm.features.http;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by Administrator on 2018/1/4.
 */

public interface SendLocationService {
    @POST("/app/getLocationData")
    public Call<ResponseBody> sendLocationData(@Body RequestBody body);
}
