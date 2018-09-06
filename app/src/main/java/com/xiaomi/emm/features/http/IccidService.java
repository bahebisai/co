package com.xiaomi.emm.features.http;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by Administrator on 2017/9/15.
 */

public interface IccidService {
    @POST("/app/updatePhone")
    public Call<ResponseBody> sendIccid(@Body RequestBody body);
}
