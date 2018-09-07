package com.xiaomi.emm.features.http;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

/**
 * Created by Administrator on 2018/2/27.
 */

public interface AppVersionService {
    @GET("/app/updateAppVersion")
    public Call<ResponseBody> sendUpdateAppVersion(@Query("alias") String alias, @Query("appVersion") String appVersion);

    @GET()
    Call<ResponseBody> getInfo(@Url String url, @QueryMap Map<String, String> map);
}
