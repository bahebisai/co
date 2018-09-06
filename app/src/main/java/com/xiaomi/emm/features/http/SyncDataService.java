package com.xiaomi.emm.features.http;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


/**
 * Created by Administrator on 2017/10/13.
 */

public interface SyncDataService {
    @GET("/app/syncAPPdata")
    public Call<ResponseBody> syncData(@Query("alias") String alias);
}
