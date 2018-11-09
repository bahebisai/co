package com.zoomtech.emm.features.event;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import okhttp3.ResponseBody;

/**
 * Created by Administrator on 2017/5/26.
 */
public class LoginEvent extends BaseEvent<ResponseBody> {

    /**
     * @param uuid 唯一识别码
     */
    public LoginEvent(@Nullable String uuid) {
        super(uuid);
    }

    /**
     * @param uuid  唯一识别码
     * @param code  网络返回码
     * @param responseBody 返回
     */
    public LoginEvent(@Nullable String uuid, @NonNull Integer code, @Nullable ResponseBody responseBody) {
        super(uuid,code,responseBody);
    }

}
