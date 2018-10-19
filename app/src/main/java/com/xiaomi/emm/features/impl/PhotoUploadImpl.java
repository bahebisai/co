package com.xiaomi.emm.features.impl;

import android.content.Context;
import android.util.Log;

import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.definition.UrlConst;
import com.xiaomi.emm.features.http.RequestService;
import com.xiaomi.emm.features.luban.PhotoUploadListener;
import com.xiaomi.emm.utils.DataParseUtil;
import com.xiaomi.emm.utils.HttpHelper;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.utils.PreferencesManager;
import com.xiaomi.emm.utils.TheTang;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Administrator on 2017/11/9.
 */

public class PhotoUploadImpl extends BaseImpl<RequestService>{

    private static final String TAG = "PhotoUploadImpl";
    Context mContext;

    public PhotoUploadImpl(Context context) {
        super();
        this.mContext = context;
    }

    /**
     * 上传图片
     * @param file
     * @param photoUploadListener
     * @param photoType 1、头像 2、背景
     */
    public void photoUpload(File file, final PhotoUploadListener photoUploadListener,String photoType) {

        String alias = PreferencesManager.getSingleInstance().getData( Common.alias );

        JSONObject json = new JSONObject();
        try {
            json.put( "alias", alias );
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody description = RequestBody.create( MediaType.parse( "multipart/form-data" ), json.toString() );

        RequestBody requestFile = RequestBody.create( MediaType.parse( "multipart/form-data" ), file );

        MultipartBody.Part body = MultipartBody.Part.createFormData( "file", file.getName(), requestFile );

//        mService.photoUpload( body, description ).enqueue( new Callback<ResponseBody>() {
        mService.uploadPhoto(UrlConst.USER_AVATAR, description, body).enqueue(new Callback<ResponseBody>() {//todo baii
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                String content = HttpHelper.getResponseBodyString(response);

                if (HttpHelper.whetherSendSuccess(content)) {
                    photoUploadListener.onSuccess();
                } else {

                    JSONObject object = null;
                    String resultCode = null;

                    try {
                        object = new JSONObject(content);
                        resultCode = object.getString("result");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    photoUploadListener.onError("1", resultCode );//400

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                photoUploadListener.onError("2","");
            }
        } );
    }
}
