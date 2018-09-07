package com.xiaomi.emm.features.impl;

import android.content.Context;

import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.definition.UrlConst;
import com.xiaomi.emm.features.http.RequestService;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.utils.PreferencesManager;
import com.xiaomi.emm.utils.TheTang;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Administrator on 2017/9/12.
 */

public class SwitchLogImpl extends BaseImpl<RequestService> {
    private static final String TAG = "SwitchLogImpl";
    Context mContext;

    public SwitchLogImpl(Context context) {
        super();
        this.mContext = context;
    }

    public void sendSwitchLog( String log ) {


        RequestBody body = RequestBody.create( okhttp3.MediaType.parse(
                "application/json;charset=UTF-8" ), jsonSwitchLog(log).toString() );

//        mService.sendSwitchLog( body ).enqueue( new Callback<ResponseBody>() {
        mService.uploadInfo(UrlConst.SWITCH_LOG, body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (TheTang.getSingleInstance().whetherSendSuccess( response )) {
                    PreferencesManager.getSingleInstance().removeLogData("switchLog");//清除数据
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        } );
    }

    /**
     * 解析Switch Log
     * @param log
     * @return
     */
    private JSONObject jsonSwitchLog(String log) {

        JSONObject logObject = new JSONObject(  );
        try {
            logObject.put( "alias", PreferencesManager.getSingleInstance().getData( Common.alias ) );

            JSONArray jsonArray = new JSONArray(  );

            String[] logList = log.split( "," );

            for ( int i = 0; i < logList.length; i++) {
                JSONObject switchLog = new JSONObject(  );

                String[] logString = logList[i].split( "/" );

                switchLog.put( "create_time", logString[0] );
                switchLog.put( "type", logString[1] );
                switchLog.put( "switch_direction", logString[2] );
                jsonArray.put( switchLog );
            }

            logObject.put( "list", jsonArray );

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return logObject;
    }

}
