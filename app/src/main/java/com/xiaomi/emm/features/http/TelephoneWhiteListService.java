package com.xiaomi.emm.features.http;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface TelephoneWhiteListService {
    @POST("/user/getPhoneContact")
    public Call<ResponseBody> getTelephoneWhiteList(@Body RequestBody body);
}
