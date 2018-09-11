package com.xiaomi.emm.features.impl;

import android.content.Context;
import android.util.ArrayMap;

import com.google.gson.Gson;
import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.definition.UrlConst;
import com.xiaomi.emm.features.db.DatabaseOperate;
import com.xiaomi.emm.features.http.RequestService;
import com.xiaomi.emm.features.resend.MessageResendManager;
import com.xiaomi.emm.model.MessageSendData;
import com.xiaomi.emm.utils.PreferencesManager;
import com.xiaomi.emm.utils.TheTang;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Administrator on 2017/8/22.
 */

public class SendMessageManager extends BaseImpl<RequestService> {
    private static final String TAG = SendMessageManager.class.getName();
    Context mContext;

    public SendMessageManager(Context context) {
        super();
        this.mContext = context;
    }

    public void sendMessage(MessageSendData data) {
        int sendCode = data.getOrderCode();
        String json = data.getJsonContent();
        String url = getUrl(sendCode);
        Call<ResponseBody> responseBodyCall = null;
        switch (sendCode) {
            /***************** Post String **********************************/
            case Common.coming_number_impl:
            case Common.device_impl:
            case Common.excute_complete_impl:
            case Common.feedback:
            case Common.iccid_impl:
            case Common.location_upload:
            case Common.login:
            case Common.SMS_BACKUP:
            case Common.switch_log_impl:
            case Common.get_telephone_white:
            case Common.USER_TRACK:
                RequestBody body = RequestBody.create(okhttp3.MediaType.parse(
                        "application/json;charset=UTF-8"), json);
                responseBodyCall = mService.uploadInfo(url, body);
                break;

            /***************** Post String & File **********************************/
            case Common.CALL_RECORDER_BACKUP:
            case Common.log_upload_impl:
                RequestBody description = RequestBody.create(okhttp3.MediaType.parse(
                        "application/json;charset=UTF-8"), json);
                String filePath = "";
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    filePath = jsonObject.getString("filePath");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                File file = new File(filePath);
                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                MultipartBody.Part fileBody = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
                responseBodyCall = mService.uploadInfo(url, description, fileBody);
                break;
            case Common.USER_AVATAR_UPLOAD:
                //todo baii
                break;

            /***************** Get **********************************/
            case Common.device_update:
            case Common.SETTING_DATA:
                responseBodyCall = mService.getInfo(url);
                break;

            /***************** Get with Query **********************************/
            case Common.app_impl:
            case Common.password_impl:
            case Common.sd_card:
            case Common.machine_card:
            case Common.APP_VERSION_UPDATE:
            case Common.white_telephone_status:
                Gson gson = new Gson();
                Map<String, String> map = gson.fromJson(json, Map.class);
                responseBodyCall = mService.getInfo(url, map);
                break;
                //todo baii getStreamInfo & downloadFile in RequestService
            default:
                break;
        }

        MessageSendListener sendListener = data.getSendListener();
        MessageResendManager.MessageResendListener resendListener = data.getResendListener();
        if (responseBodyCall != null) {
            responseBodyCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (!TheTang.getSingleInstance().whetherSendSuccess(response)) {
                        if (data.needResend()) {
                            DatabaseOperate.getSingleInstance().add_backResult_sql(sendCode + "", json);
                        }
                        if (sendListener != null) {
                            sendListener.onError();
                        }
                        if (resendListener != null) {
                            resendListener.resendError();
                        }
                    } else {
                        if (sendListener != null) {
                            sendListener.onSuccess();
                        }
                        if (resendListener != null) {
                            resendListener.resendSuccess();
                        }
                    }

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    if (data.needResend()) {
                        DatabaseOperate.getSingleInstance().add_backResult_sql(sendCode + "", json);
                    }
                    if (sendListener != null) {
                        sendListener.onFailure();
                    }
                    if (resendListener != null) {
                        resendListener.resendFail();
                    }
                }
            });
        }
    }

    private String getUrl(int sendCode) {
        String url;
        switch (sendCode) {
            case Common.coming_number_impl:
                url = UrlConst.CALL_LOG;
                break;
            case Common.device_impl:
                url = UrlConst.DEVICE_INFO;
                break;
            case Common.excute_complete_impl:
                url = UrlConst.EXE_COMPLETE;
                break;
            case Common.feedback:
                url = UrlConst.FEEDBACK;
                break;
            case Common.iccid_impl:
                url = UrlConst.PHONE_INFO;
                break;
            case Common.location_upload:
                url = UrlConst.LOCATION_DATA;
                break;
            case Common.login:
                url = UrlConst.LOGIN;
                break;
            case Common.SMS_BACKUP:
                url = UrlConst.SMS;
                break;
            case Common.switch_log_impl:
                url = UrlConst.SWITCH_LOG;
                break;
            case Common.get_telephone_white:
                url = UrlConst.PHONE_CONTACTS;
                break;
            case Common.USER_TRACK:
                url = UrlConst.USER_TRACK;
                break;
            case Common.CALL_RECORDER_BACKUP:
                url = UrlConst.CALL_RECORDER;
                break;
            case Common.log_upload_impl:
                url = UrlConst.LOG_UPLOAD;
                break;
            case Common.USER_AVATAR_UPLOAD:
                url = UrlConst.USER_AVATAR;
                break;
            case Common.device_update:
                url = UrlConst.DEVICE_UPDATE;
                break;
            case Common.SETTING_DATA:
                url = UrlConst.SETTING_DATA;
                break;
            case Common.app_impl:
                url = UrlConst.APP_COMPLIANCE;
                break;
            case Common.password_impl:
                url = UrlConst.FEEDBACK_PASSWORD;
                break;
            case Common.sd_card:
            case Common.machine_card:
                url = UrlConst.SYSTEM_COMPLIANCE;
                break;
            case Common.APP_VERSION_UPDATE:
                url = UrlConst.APP_UPDATE_VERSION;
                break;
            case Common.white_telephone_status:
                url = UrlConst.TELE_WHITELIST_STATUS;
                break;
            default:
                url = "";
        }
        return url;
    }

    public interface MessageSendListener{
        void onSuccess();
        void onFailure();
        void onError();
    }
}
