package com.xiaomi.emm.features.http;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by Administrator on 2018/1/9.
 */

public interface ExcuteCompleteService {

    @POST("/app/getCommandImplementResult")
    public Call<ResponseBody> sendExcuteComplete(@Body RequestBody feedBack);
}
