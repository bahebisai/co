package com.xiaomi.emm.view.listener;

import com.xiaomi.emm.model.DownLoadEntity;

/**
 * Created by Administrator on 2017/7/17.
 */

public interface DownLoadTaskListener {
    void onStart(String packageName);

    void onCancel(DownLoadEntity downLoadEntity);

    void onDownLoading(DownLoadEntity downLoadEntity);

    void onCompleted(DownLoadEntity downLoadEntity);

    void onError(DownLoadEntity downLoadEntity);
}
