package com.xiaomi.emm.features.impl;

import android.content.Context;
import android.util.Log;

import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.features.http.TrajectoryService;
import com.xiaomi.emm.utils.PreferencesManager;
import com.xiaomi.emm.utils.TheTang;

import org.json.JSONObject;

import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Administrator on 2018/1/4.
 */

public class TrajectoryImpl extends BaseImpl<TrajectoryService> {
    private static final String TAG = "LocationImpl";
    Context mContext;

    String alias = PreferencesManager.getSingleInstance().getData( Common.alias );

    public TrajectoryImpl(Context context) {
        super();
        this.mContext = context;
    }

    public void sendTrajectoryData(final Map<String, String> map) {

        JSONObject json = new JSONObject(map);
        Log.w(TAG,"发送轨迹 数据----"+map.toString() );
        RequestBody description = RequestBody.create( MediaType.parse(
                "application/json;charset=UTF-8" ), json.toString() );

        mService.sendTrajectoryData( description ).enqueue( new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!TheTang.getSingleInstance().whetherSendSuccess(response)) {
                    Log.w(TAG,"发送轨迹 fail!" );
                } else {
                    Log.w(TAG,"发送轨迹 success!");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.w(TAG,"发送轨迹 onFailure!");
            }
        } );
    }
}
