package com.xiaomi.emm.features.http;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface TrajectoryService {
    @POST("/userTrack/upload")
    public Call<ResponseBody> sendTrajectoryData(@Body RequestBody body);
}
