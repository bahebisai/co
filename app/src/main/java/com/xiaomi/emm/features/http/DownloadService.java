package com.xiaomi.emm.features.http;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

/**
 * Created by Administrator on 2017/7/6.
 */

public interface DownloadService {
    @Streaming
    @GET("/web/apps/download")
    public /*Observable*/Call<ResponseBody> downloadFile(@Header("Range") String range, @Query("alias") String alias, @Query("id") String id, @Query("code") String type);
    //public Observable<ResponseBody> /*Call<ResponseBody>*/ downloadFile(@Url String url);
    //public Call<ResponseBody> downloadFile(@Url String url);
}
