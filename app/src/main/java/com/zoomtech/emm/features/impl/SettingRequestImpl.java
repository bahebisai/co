package com.zoomtech.emm.features.impl;

import android.content.Context;

import com.zoomtech.emm.definition.UrlConst;
import com.zoomtech.emm.features.http.RequestService;
import com.zoomtech.emm.utils.DataParseUtil;
import com.zoomtech.emm.utils.HttpHelper;
import com.zoomtech.emm.features.presenter.TheTang;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Administrator on 2018/3/21.
 */
//todo baii impl for reponse info
public class SettingRequestImpl extends BaseImpl<RequestService> {
    private static final String TAG = "AppImpl";
    Context mContext;

    public SettingRequestImpl(Context context) {
        super();
        this.mContext = context;
    }

    public void getSettingData() {
//        mService.getSettingData().enqueue( new Callback<ResponseBody>() {
        mService.getInfo(UrlConst.SETTING_DATA).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                final String content = HttpHelper.getResponseBodyString(response);

                if (HttpHelper.whetherSendSuccess(content)) {
                    TheTang.getSingleInstance().getThreadPoolObject().submit( new Runnable() {
                        @Override
                        public void run() {
                            DataParseUtil.jsonSettingData( content );
                        }
                    } );
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        } );
    }
}
