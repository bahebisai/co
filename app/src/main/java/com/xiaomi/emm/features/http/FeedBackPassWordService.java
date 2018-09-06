package com.xiaomi.emm.features.http;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Administrator on 2017/10/16.
 */

public interface FeedBackPassWordService {
    @GET("app/setPassword")
    public Call<ResponseBody> feedbackPassword(@Query( "alias" ) String alias, @Query( "password" ) String password);
}
