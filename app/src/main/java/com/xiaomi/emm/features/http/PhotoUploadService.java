package com.xiaomi.emm.features.http;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Created by Administrator on 2017/11/9.
 */

public interface PhotoUploadService {
    @Multipart
    @POST("/user/headPhotoUpload")
    public Call<ResponseBody> photoUpload(@Part MultipartBody.Part file, @Part("alias") RequestBody description);
}
