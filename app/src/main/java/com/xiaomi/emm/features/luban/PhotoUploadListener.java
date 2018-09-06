package com.xiaomi.emm.features.luban;

import java.io.File;

/**
 * Created by Administrator on 2017/11/9.
 */

public interface PhotoUploadListener {

    /**
     * 上传成功
     */
    void onSuccess();

    /**
     * errorType 错误原因
     * 1：照片不合法
     * 2：上传失败
     * @param errorType
     */
    void onError(String errorType, String message);

}
