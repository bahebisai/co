package com.xiaomi.emm.features.http;

import java.io.File;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by Administrator on 2017/9/20.
 */

public interface LogUpLoadService {
    @Multipart
    @POST("/log/uploadDebugLog")
    public Call<ResponseBody> logUpload(@Part("description") RequestBody description, @Part MultipartBody.Part file);
}
