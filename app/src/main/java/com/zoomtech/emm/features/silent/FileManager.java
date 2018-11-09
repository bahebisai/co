package com.zoomtech.emm.features.silent;

import com.zoomtech.emm.base.BaseApplication;
import com.zoomtech.emm.definition.OrderConfig;
import com.zoomtech.emm.features.db.DatabaseOperate;
import com.zoomtech.emm.features.event.APKEvent;
import com.zoomtech.emm.features.event.AvatarUpdateEvent;
import com.zoomtech.emm.features.event.CompleteEvent;
import com.zoomtech.emm.features.event.NotifyEvent;
import com.zoomtech.emm.model.DownLoadEntity;
import com.zoomtech.emm.utils.FileUtils;
import com.zoomtech.emm.features.presenter.TheTang;
import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2017/8/3.
 */

public class FileManager{
    private final static String TAG = "FileManager";
    private DownLoadEntity downLoadEntity;

    public FileManager(DownLoadEntity downLoadEntity) {
        this.downLoadEntity = downLoadEntity;
    }

    /**
     * 执行文件后续处理
     */
    public void run() {
        //清除下载记录
        //DatabaseOperate.getSingleInstance().deleteDownLoadFile( downLoadEntity );

        if ( "0".equals( downLoadEntity.type ) ) {
            final APKEvent event = new APKEvent(downLoadEntity, OrderConfig.SilentInstallAppication);

            AppTask appTask = new AppTask();
            appTask.onSilentExcutor(event);

        } else if ( "1".equals( downLoadEntity.type ) ) {

            DatabaseOperate.getSingleInstance().deleteDownLoadFile( downLoadEntity );

            FileUtils.renameFile( BaseApplication.baseFilesPath, downLoadEntity.saveName,
                    downLoadEntity.saveName.substring( 5, downLoadEntity.saveName.length() ) );

            downLoadEntity.saveName = downLoadEntity.saveName.substring( 5, downLoadEntity.saveName.length() );

            DatabaseOperate.getSingleInstance().insertFile(downLoadEntity);

            TheTang.getSingleInstance().addMessage(String.valueOf(OrderConfig.IssuedFile),downLoadEntity.saveName);

            //下载成功
            EventBus.getDefault().post( new CompleteEvent( downLoadEntity.code, "true", downLoadEntity.sendId) );
            EventBus.getDefault().post(new NotifyEvent());
        } else if ( "2".equals( downLoadEntity.type ) ) {
            DatabaseOperate.getSingleInstance().deleteDownLoadFile( downLoadEntity );

            FileUtils.renameFile( BaseApplication.baseImagesPath, downLoadEntity.saveName,
                    downLoadEntity.saveName.substring( 5, downLoadEntity.saveName.length() ) );

            //同步成功
            EventBus.getDefault().post( new CompleteEvent( downLoadEntity.code, "true", downLoadEntity.sendId) );
            EventBus.getDefault().post(new AvatarUpdateEvent());
        }
    }
}
