package com.xiaomi.emm.features.http;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by lenovo on 2017/9/5.
 */

public interface WebclipImageService {
    @Streaming
    @GET
    Call<ResponseBody> downloadPicFromNet(@Url String fileUr);//l//TODO BAII



}
