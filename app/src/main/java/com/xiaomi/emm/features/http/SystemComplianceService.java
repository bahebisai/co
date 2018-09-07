package com.xiaomi.emm.features.http;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

/**
 * Created by Administrator on 2017/8/15.
 */

public interface SystemComplianceService {
    @GET("web/systemCompliance/compliance")
    public Call<ResponseBody> systemCompliance(@Query("alias") String alias, @Query("systemComplianceId") int systemComplianceId,  @Query("state") String state, @Query("type") String type);

    @GET()
    Call<ResponseBody> getInfo(@Url String url, @QueryMap Map<String, String> map);
}
