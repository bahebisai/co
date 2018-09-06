package com.xiaomi.emm.features.http;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by Administrator on 2018/3/21.
 */

public interface SettingRequestService {
    @GET("/clientManagement/get")
    public Call<ResponseBody> getSettingData();
}
