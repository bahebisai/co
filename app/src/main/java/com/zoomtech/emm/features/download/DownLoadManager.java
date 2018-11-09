package com.zoomtech.emm.features.download;

import android.util.Log;
import com.zoomtech.emm.features.db.DatabaseOperate;
import com.zoomtech.emm.model.DownLoadEntity;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Duan on 17/7/15.
 */

public class DownLoadManager {

    private final static String TAG = "DownLoadManager";

    private DownLoadManager() {
    }

    private static class DownLoadManagerHolder {
        private static final DownLoadManager INSTANCE = new DownLoadManager();
    }

    public static final DownLoadManager getInstance() {
        return DownLoadManagerHolder.INSTANCE;
    }

    /**
     * 下载同步
     */
    public /*synchronized */void download(List<DownLoadEntity> list) {

        List<DownLoadEntity> mList = new ArrayList<>();

        //如果传入的数据为空，查询数据库是否有未下载完成的文件
        if (list == null) {
            mList = DatabaseOperate.getSingleInstance().queryAllDownLoadFile();
            Log.w( TAG, "mList.size = " + mList.size());
            if (mList.isEmpty()) {
                return;
            }
        } else {

            for (DownLoadEntity entity : list) {

                DownLoadEntity downLoadEntity = DatabaseOperate.getSingleInstance().queryDownLoadFile(entity.app_id);

                if (downLoadEntity == null) {
                    //将预下载文件app_id存入数据库
                    if ("0".equals( entity.type )) {
                        DatabaseOperate.getSingleInstance().insertDownLoadFile( entity.app_id, entity.sendId, entity.code, 0,
                                0, 0, "", entity.packageName, entity.type, entity.internet, entity.uninstall );
                        Log.w( TAG, "insertDownLoadFile = " + entity.packageName );
                    } else {
                        DatabaseOperate.getSingleInstance().insertDownLoadFile( entity.app_id, entity.sendId, entity.code, 0,
                                0, 0, "", "", entity.type, "", "" );
                    }

                    mList.add(entity);
                } else {
                    mList.add(downLoadEntity);
                }
            }
        }

        DownLoadRequest mDownLoadRequest = DownLoadRequest.getSingleInstance();
        mDownLoadRequest.setData( mList);
        mDownLoadRequest.start();
    }
}
