package com.xiaomi.emm.features.http;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by Administrator on 2017/9/27.
 */

public interface DeviceUpdateService {
    @GET("/setUp/download")
    public Call<ResponseBody> deviceUpdate();
}
