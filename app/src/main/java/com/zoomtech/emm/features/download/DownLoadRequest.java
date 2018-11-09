package com.zoomtech.emm.features.download;

import android.text.TextUtils;
import android.util.Log;
import com.zoomtech.emm.base.BaseApplication;
import com.zoomtech.emm.features.db.DatabaseOperate;
import com.zoomtech.emm.features.event.CompleteEvent;
import com.zoomtech.emm.features.silent.FileManager;
import com.zoomtech.emm.view.listener.DownLoadTaskListener;
import com.zoomtech.emm.model.DownLoadEntity;
import org.greenrobot.eventbus.EventBus;
import java.io.File;
import java.util.List;

/**
 * 下载逻辑管理类
 * Created by Duan on 17/7/15.
 */
public class DownLoadRequest {

    public static int repeatCount = 10;
    DownLoadTask downLoadTask;
    List<DownLoadEntity> mList;
    DownLoadRequestListener mDownLoadRequestListener;

    //单例
    private volatile static DownLoadRequest mDownLoadRequest;

    private DownLoadRequest() {
    }

    /**
     * 单例
     *
     * @return
     */
    public static DownLoadRequest getSingleInstance() {
        if (null == mDownLoadRequest) {
            synchronized (DownLoadRequest.class) {
                if (null == mDownLoadRequest) {
                    mDownLoadRequest = new DownLoadRequest();
                }
            }
        }
        return mDownLoadRequest;
    }

    /**
     * 设置数据
     * @param mList
     */
    public void setData(List<DownLoadEntity> mList) {
        this.mList = mList;
    }

    /**
     * 执行下载请求
     */
    public void start() {

        if (mList == null || mList.size() < 1)
            return;

        for (DownLoadEntity entity : mList) {
            //使数据库存的已下载大小与实际大小保持一致
            if (!TextUtils.isEmpty( entity.saveName )) {
                verificationFileData( entity );
            }

            addDownLoadTask( entity );
        }
    }

    /**
     * 判断实际下载的文件大小是否与数据库存储的大小一致
     *
     * @param entity
     */
    private void verificationFileData(DownLoadEntity entity) {

        File futureFile = null;
        String path = null;

        if ("0".equals( entity.type )) {

            path = BaseApplication.baseAppsPath;
            futureFile = new File( path + File.separator + entity.saveName );

        } else if ("1".equals( entity.type )) {

            path = BaseApplication.baseFilesPath;
            futureFile = new File( path + File.separator + entity.saveName );

        } else if ("2".equals( entity.type )) {

            File file = new File( BaseApplication.baseFilesPath + "/images/" );
            if (!file.exists()) {
                file.mkdir();
            }
            //存储个人头像
            path = BaseApplication.baseFilesPath + "/images/personPic.png";
            futureFile = new File( path );

        }

        if (futureFile.exists()) {
            long downloadedFileSize = 0;
            downloadedFileSize = futureFile.length();

            //判断实际下载的文件大小是否与数据库存储的大小一致
            if (entity.downed > 0 && downloadedFileSize != entity.downed) {
                // 比较激进的容错方案.
                entity.downed = downloadedFileSize;
            }
        }
    }

    /**
     * 添加下载任务
     *
     * @param downLoadEntity
     */
    private void addDownLoadTask(DownLoadEntity downLoadEntity) {
        mDownLoadRequestListener = new DownLoadRequestListener();
        Log.w( "DownLoadRequest", "addDownLoadTask = " + downLoadEntity.downed );
        if (downLoadEntity.downed > 0) {
            Log.w( "DownLoadRequest", "addDownLoadTask insert! " );
            downLoadTask = new DownLoadTask.Builder().downLoadModel( downLoadEntity )
                    .downLoadTaskListener( mDownLoadRequestListener ).build();
            executeNetWork( downLoadEntity, downLoadTask );
        } else {
            createDownLoadTask( downLoadEntity, mDownLoadRequestListener );
        }
    }

    /**
     * 创建下载线程
     *
     * @param downLoadEntity
     * @param loadRequestListener
     */
    public void createDownLoadTask(DownLoadEntity downLoadEntity, DownLoadRequestListener loadRequestListener) {

        downLoadTask = new DownLoadTask.Builder().downLoadModel( downLoadEntity )
                .downLoadTaskListener( loadRequestListener ).build();
        executeNetWork( downLoadEntity, downLoadTask );
    }

    private void executeNetWork(DownLoadEntity entity, DownLoadTask downLoadTask) {
        downLoadTask.run();
    }

    //下载结果监听
    private class DownLoadRequestListener implements DownLoadTaskListener {

        @Override
        public void onStart(String packageName) {
        }

        @Override
        public void onCancel(DownLoadEntity downLoadEntity) {
        }

        @Override
        public void onDownLoading(DownLoadEntity downLoadEntity) {
            DatabaseOperate.getSingleInstance().updateDownLoadFile( downLoadEntity );
        }

        @Override
        public void onCompleted(DownLoadEntity downLoadEntity) {
            Log.w( "DownLoadRequest", "onCompleted = " + downLoadEntity.saveName );

            //应用下载完成则恢复到无应用下载状态
            if("0".equals( downLoadEntity.type )){

                /*TheTang.getSingleInstance().renameFile( BaseApplication.baseAppsPath, downLoadEntity.saveName,
                        downLoadEntity.saveName.substring( 5, downLoadEntity.saveName.length() ) );

                downLoadEntity.saveName = downLoadEntity.saveName.substring( 5, downLoadEntity.saveName.length() );*/

                EventBus.getDefault().post( new CompleteEvent( downLoadEntity.code, "true", downLoadEntity.sendId) );
            }

            //文件处理
            FileManager fileManager = new FileManager( downLoadEntity );
            fileManager.run();
        }

        @Override
        public void onError(DownLoadEntity downLoadEntity) {
            DatabaseOperate.getSingleInstance().updateDownLoadFile( downLoadEntity );
            if (!repeatTask( repeatCount, downLoadEntity )) {
                if (repeatCount <= 0) {
                    return;
                }
            } else {
                repeatCount--;
            }
        }
    }

    /**
     * 失败重复
     *
     * @param repeatCount
     * @param downLoadEntity
     * @return
     */
    private boolean repeatTask(int repeatCount, DownLoadEntity downLoadEntity) {
        if (downLoadEntity.downed < downLoadEntity.total && repeatCount > 0) {
            downLoadTask = new DownLoadTask.Builder().downLoadModel( downLoadEntity )
                    .downLoadTaskListener( this.mDownLoadRequestListener ).build();
            executeNetWork( downLoadEntity, downLoadTask );
            return true;
        }

        return false;
    }
}
