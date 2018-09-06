package com.xiaomi.emm.features.http;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by Administrator on 2017/9/20.
 */

public interface CallRecorderUploadService {
    @Multipart
    @POST("/userSoundStrategy/upload")
    public Call<ResponseBody> uploadCallRecorder(@Part("description") RequestBody description, @Part MultipartBody.Part file);
}
