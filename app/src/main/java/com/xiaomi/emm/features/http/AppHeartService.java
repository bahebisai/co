package com.xiaomi.emm.features.http;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Administrator on 2017/9/19.
 */

public interface AppHeartService {
    @GET("/app/getDelayCode")
    public Call<ResponseBody> sendDelayCode(@Query("alias") String alias);
}
