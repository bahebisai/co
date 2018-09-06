package com.xiaomi.emm.features.http;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WhiteTelephoneService {
    @GET("/app/updateUserTelephone")
    public Call<ResponseBody> sendWhiteTelephoneStatus(@Query( "status" ) String status);
}
