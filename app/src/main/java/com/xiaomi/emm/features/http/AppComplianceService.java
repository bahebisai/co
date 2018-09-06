package com.xiaomi.emm.features.http;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Administrator on 2017/8/22.
 */

public interface AppComplianceService {
    @GET("web/appCompliance/compliance")
    public Call<ResponseBody> appCompliance(@Query("alias") String alias, @Query("appComplianceId") int appComplianceId, @Query("type") String type, @Query("names") String names);
}


