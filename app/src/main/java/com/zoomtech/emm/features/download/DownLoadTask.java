package com.zoomtech.emm.features.download;

import android.text.TextUtils;
import android.util.Log;
import com.zoomtech.emm.base.BaseApplication;
import com.zoomtech.emm.definition.Common;
import com.zoomtech.emm.features.db.DatabaseOperate;
import com.zoomtech.emm.features.event.CompleteEvent;
import com.zoomtech.emm.features.http.RequestService;
import com.zoomtech.emm.features.impl.BaseImpl;
import com.zoomtech.emm.features.presenter.MDM;
import com.zoomtech.emm.features.manager.PreferencesManager;
import com.zoomtech.emm.features.presenter.TheTang;
import com.zoomtech.emm.view.listener.DownLoadTaskListener;
import com.zoomtech.emm.model.DownLoadEntity;
import com.zoomtech.emm.utils.LogUtil;
import org.greenrobot.eventbus.EventBus;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Duan on 17/7/15.
 */

public class DownLoadTask extends BaseImpl<RequestService> /*implements Runnable*/ {
    public static final String TAG = "DownLoadTask";
    DownLoadEntity downLoadEntity = null;
    DownLoadTaskListener mDownLoadTaskListener = null;
    String path = null;
    String filename = null;

    private DownLoadTask(DownLoadEntity downLoadEntity, DownLoadTaskListener mDownLoadTaskListener) {
        super();
        this.downLoadEntity = downLoadEntity;
        this.mDownLoadTaskListener = mDownLoadTaskListener;
    }

    //@Override
    public void run() {

        if (mService == null) {
            return;
        }

        try {

            String rang = "bytes=" + downLoadEntity.downed  + "-";

            mService.downloadFile( rang, PreferencesManager.getSingleInstance().getData( Common.alias ),
                    downLoadEntity.app_id, downLoadEntity.code )
                    .enqueue( new Callback<ResponseBody>() {
                                  @Override
                                  public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {

                                      Log.w( TAG, "response = " + response  );
                                      TheTang.getSingleInstance().getThreadPoolObjectForDownload().submit( new Runnable() {
                                          @Override
                                          public void run() {
                                              writeResponseBodyToDisk( response, downLoadEntity );
                                          }
                                      } );
                                  }
                                  @Override
                                  public void onFailure(Call<ResponseBody> call, Throwable t) {
                                      mDownLoadTaskListener.onError( downLoadEntity );
                                  }
                              }
                    );
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "DownLoad File " + e.getCause().toString() );
        }
    }

    public void writeResponseBodyToDisk(Response<ResponseBody> body, DownLoadEntity downLoadEntity) {

        /**
         * 解决请求参数不对的问题
         */
        int code = body.code();

        if (400 <= code && code <= 500) {
            downLoadError();
            return;
        }

        if (body.body() == null) {
            LogUtil.writeToFile( TAG, "DownLoad ResponseBody is null," + "code = " + downLoadEntity.code + "id ＝ " + downLoadEntity.app_id );
            downLoadError();
            return;
        }

        //获得文件名
        try {
            if ("2".equals( downLoadEntity.type ) ) {
                filename = "temp_" + "personPic.png";
            } else {
                filename = getFileName( body );
            }
        } catch (Exception exception) {

            LogUtil.writeToFile( TAG, "File Name is unKnow " + exception.toString() );
            downLoadError();
            return;
        }

        downLoadEntity.saveName = filename;

        //获得文件路径
        path = getFilePath( );

        LogUtil.writeToFile( TAG, "File Name is " + filename + " app_id  " + downLoadEntity.app_id );

        //获得RandomAccessFile
        RandomAccessFile savedFile = null;
        savedFile = createRandomAccessFile( path, filename );

        long fileSize = 0;
        try {
            fileSize = body.body().contentLength();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "Exception1 = " + e.getCause().toString() );
        }

        LogUtil.writeToFile( TAG, "contentLength = " + (long) body.body().contentLength() );

        //如果下载中的body的大小为-1，表示传递的数据内容为空，丢弃
        if (fileSize == -1) {
            downLoadError();
            return;
        }

        if (fileSize == 0) {
            onCompleted();
            return;
        }

        try {
            downLoadEntity.total = (long) fileSize + savedFile.length();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //写入数据到文件
        readToFile( savedFile, body, fileSize );
    }

    /**
     * 下载出现无法解决的错误
     */
    private void downLoadError() {
        EventBus.getDefault().post( new CompleteEvent( downLoadEntity.code, "false", downLoadEntity.sendId ) );
        DatabaseOperate.getSingleInstance().deleteDownLoadFile( downLoadEntity );
        MDM.deleteFile( new File( BaseApplication.baseAppsPath + File.separator + downLoadEntity.saveName ) );
    }

    /**
     * 获得Content-Disposition中的filename值
     *
     * @param body
     * @return
     */
    private static String getFileName(Response<ResponseBody> body) {
        String filename = null;
        okhttp3.Response response = body.raw();
        String disposition = response.headers().get( "Content-Disposition" ).toString();
        int position = disposition.indexOf( "=" );
        filename = "temp_" + disposition.substring( position + 1, disposition.length() );
        return filename;
    }

    /**
     * 获取文件路径
     *
     * @return
     */
    private String getFilePath( ) {
        String path = null;
        Log.w( TAG, "downLoadEntity.type = " + downLoadEntity.type );
        if ("0".equals( downLoadEntity.type )) {
            path = BaseApplication.baseAppsPath;
        } else if ("1".equals( downLoadEntity.type )) {
            path = BaseApplication.baseFilesPath;
        } else if ("2".equals( downLoadEntity.type )) {
            path = BaseApplication.baseImagesPath;//存储个人头像
        }
        return path;
    }

    /**
     * 创建RandomAccessFile
     * 描述：RandomAccessFile类专门对文件内容进行操作
     * @param path
     * @param filename
     * @return
     */
    private RandomAccessFile createRandomAccessFile(String path, String filename) {

        File pathFile = new File( path );
        if (!pathFile.exists()) {
            pathFile.mkdir();
        }

        File futureFile = new File( path + File.separator + filename);
        if (!futureFile.exists()) {
            try {
                futureFile.createNewFile();
            } catch (IOException e) {
                LogUtil.writeToFile( TAG, "futureFile " + e.getCause().toString() );
            }
        }

        RandomAccessFile savedFile = null;
        try {
            savedFile = new RandomAccessFile( futureFile, "rw" );
        } catch (FileNotFoundException e) {
            LogUtil.writeToFile( TAG, "RandomAccessFile " + e.getCause().toString() );
            e.printStackTrace();
        }
        return savedFile;
    }

    /**
     * 读取数据流到文件
     *
     * @param savedFile
     * @param body
     * @param fileSize
     */
    private void readToFile(RandomAccessFile savedFile, Response<ResponseBody> body, long fileSize) {

        try {
            //当前请求下载的应用大小不等于已下载的大小，表示该应用正在下载
            if ( downLoadEntity.downed != savedFile.length()) {

                LogUtil.writeToFile( TAG, "This app is downloading!" );

                return;
            }

            savedFile.seek( downLoadEntity.downed );
        } catch (IOException e) {
            e.printStackTrace();
        }

        InputStream inputStream = null;
        BufferedInputStream bufferedInputStream = null;
        try {
            try {
                byte[] fileReader = new byte[1024 * 8];
                long fileSizeDownloaded = 0;

                inputStream = body.body().byteStream();

                bufferedInputStream = new BufferedInputStream(inputStream);

                while (true) {

                    if ( bufferedInputStream != null ) {

                        int read = bufferedInputStream.read( fileReader );

                        if (read == -1) {
                            break;
                        }

                        savedFile.write( fileReader, 0, read );
                        fileSizeDownloaded += read;

                        downLoadEntity.downed = downLoadEntity.downed + read;

                        Log.w( TAG, downLoadEntity.saveName + " == " + downLoadEntity.downed );

                        onDownLoading( downLoadEntity);

                        if (downLoadEntity.total == downLoadEntity.downed) {//为解决读取死循环的问题
                            break;
                        }
                    }
                }

                if (downLoadEntity.total == downLoadEntity.downed) {
                    onCompleted();
                }

            } catch (Exception exception) {
                LogUtil.writeToFile( TAG, "Exception = " + exception.toString() );
                onError();
            } finally {
                savedFile.close();

                if (bufferedInputStream != null ) {
                    bufferedInputStream.close();
                }

                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (IOException exception) {
            Log.w( TAG, "IOException = " + exception.getCause().getMessage() );
        }
    }

    /**
     * 下载过程回调
     *
     * @param downLoadEntity
     */
    private void onDownLoading(DownLoadEntity downLoadEntity/*, long fileSizeDownloaded*/) {
        mDownLoadTaskListener.onDownLoading( downLoadEntity );
    }

    /**
     * 下载完成回调
     */
    private void onCompleted() {
        LogUtil.writeToFile( TAG, downLoadEntity.packageName + "/" + downLoadEntity.app_id + " file had download completed!" );
        mDownLoadTaskListener.onCompleted(downLoadEntity);
    }

    /**
     * 下载错误回调
     */
    private void onError() {
        mDownLoadTaskListener.onError( downLoadEntity );
    }

    /**
     * DownLoadTask的Builder类
     */
    public static final class Builder {
        private DownLoadEntity mDownModel;
        private DownLoadTaskListener mDownLoadTaskListener;

        public Builder downLoadModel(DownLoadEntity downLoadEntity) {
            mDownModel = downLoadEntity;
            return this;
        }

        public Builder downLoadTaskListener(DownLoadTaskListener downLoadTaskListener) {
            mDownLoadTaskListener = downLoadTaskListener;
            return this;
        }

        public DownLoadTask build() {
            if (TextUtils.isEmpty(mDownModel.app_id)) {
                throw new IllegalStateException( "DownLoad URL required." );
            }

            if (mDownLoadTaskListener == null) {
                throw new IllegalStateException( "DownLoadTaskListener required." );
            }
            return new DownLoadTask( mDownModel, mDownLoadTaskListener );
        }
    }
}
