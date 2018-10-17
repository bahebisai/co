package com.xiaomi.emm.features.impl;

import android.content.Context;
import android.util.Log;

import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.definition.UrlConst;
import com.xiaomi.emm.features.db.DatabaseOperate;
import com.xiaomi.emm.features.http.RequestService;
import com.xiaomi.emm.features.policy.phoneCall.CallRecorderInfo;
import com.xiaomi.emm.features.resend.MessageResendManager;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.utils.TheTang;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by Administrator on 2017/9/20.
 */

public class CallRecorderUploadImpl extends BaseImpl<RequestService> {

    public final static String TAG = "CallRecorderUploadImpl";
    Context mContext;

    public CallRecorderUploadImpl(Context context) {
        mContext = context;
    }

    public void uploadCallRecorder(String id, CallRecorderInfo info) {
//        String policyId = id;

//        /userSoundStrategy/upload
//        strategy_id ： 策略id
//        file：录音文件
//        communicationName：联系人
//        communicationNumber：电话号码
//        timeDuration：通话时长
//        type:0,被呼叫 1，拨打
//        sound_time
        JSONObject json = new JSONObject();
        try {
            json.put("strategyId", id);
            json.put("communicationName", info.getPerson());
            json.put("communicationNumber", info.getAddress());
            json.put("timeDuration", info.getmDuration());
            json.put("type", info.getType());
            json.put("soundTime", info.getDate());
            json.put("filePath", info.getPath());
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        RequestBody description = RequestBody.create( MediaType.parse( "multipart/form-data" ), json.toString() );
        final RequestBody description = RequestBody.create(okhttp3.MediaType.parse(
                "application/json;charset=UTF-8"), json.toString());
//        Log.d("baii", "upload call recorder json " + json.toString());

        File file = new File(info.getPath());
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);

        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
//        MultipartBody.Part policyId = MultipartBody.Part.createFormData("strategy_id", id);

//        mService.uploadCallRecorder(description, body).enqueue(new Callback<ResponseBody>() {
        mService.uploadInfo(UrlConst.CALL_RECORDER, description, body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d("baii", "upload call recorder json " + json.toString());
                LogUtil.writeToFile(TAG, "upload call recorder json " + json.toString());
                if (TheTang.getSingleInstance().whetherSendSuccess(response)) {
                    if (file != null && file.exists()) {
                        file.delete();
                    }
                } else {
                    DatabaseOperate.getSingleInstance().add_backResult_sql(Common.CALL_RECORDER_BACKUP + "", json.toString());
                    Log.d("baii", "call recorder upload failed 111");
                    LogUtil.writeToFile(TAG, "call recorder upload failed 111");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                DatabaseOperate.getSingleInstance().add_backResult_sql(Common.CALL_RECORDER_BACKUP + "", json.toString());
                Log.d("baii", "call recorder upload failed");
                LogUtil.writeToFile(TAG, "call recorder upload failed");
            }
        });
    }

    /**
     * 重发
     *
     * @param listener
     * @param backupJson
     */
    public void resendCallRecorder(final MessageResendManager.ResendListener listener, String backupJson) {
        Log.d("baii", "resend call recorder");
        LogUtil.writeToFile(TAG, "resend call recorder");
        final RequestBody description = RequestBody.create(okhttp3.MediaType.parse(
                "application/json;charset=UTF-8"), backupJson);
        String filePath = "";
        try {
            JSONObject jsonObject = new JSONObject(backupJson);
            filePath = jsonObject.getString("filePath");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        File file = new File(filePath);
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
//        mService.uploadCallRecorder(description, body).enqueue(new Callback<ResponseBody>() {
        mService.uploadInfo(UrlConst.CALL_RECORDER, description, body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (TheTang.getSingleInstance().whetherSendSuccess(response)) {
                    listener.onSuccess();
                    if (file != null && file.exists()) {//todo impl bai 1111111111111111111111
                        file.delete();
                    }
                } else {
                    listener.onError();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                listener.onFailure();
            }
        });
    }
}
