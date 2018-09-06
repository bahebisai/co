package com.xiaomi.emm.features.http;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by Administrator on 2017/9/6.
 */

public interface PostDeviceInfoService {
    @POST("/deviceInfo/postDeviceInfo")
    public Call<ResponseBody> postDeviceInfo(@Body RequestBody deviceInfo);
}
