package com.xiaomi.emm.features.http;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Administrator on 2017/11/21.
 */

public interface OnLineService {
    @GET("/web/missingCompliance/online")
    public Call<ResponseBody> online(@Query( "alias" ) String alias);
}
