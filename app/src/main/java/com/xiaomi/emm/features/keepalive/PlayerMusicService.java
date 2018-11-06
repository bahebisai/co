package com.xiaomi.emm.features.keepalive;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.xiaomi.emm.ITwoProcessAidlInterface;
import com.xiaomi.emm.R;
import com.xiaomi.emm.features.excute.MDMOrderService;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.features.presenter.TheTang;

/**
 * 循环播放一段无声音频，以提升进程优先级
 */

public class PlayerMusicService extends Service {
    private final static String TAG = "PlayerMusicService";
    private MediaPlayer mMediaPlayer;
    private IBinder mBilder = new RemoteBilder();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBilder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        TheTang.getSingleInstance().startForeground(this,getResources().getString(R.string.emm_runing),"EMM",14);
        LogUtil.writeToFile(TAG,TAG + "---->onCreate,启动服务");
        mMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.silent);
        mMediaPlayer.setLooping(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                startPlayMusic();
            }
        }).start();
        return START_STICKY;
    }

    private void startPlayMusic(){
        if(mMediaPlayer != null){
            mMediaPlayer.start();
        }
    }

    private void stopPlayMusic(){
        if(mMediaPlayer != null){
            mMediaPlayer.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPlayMusic();
        LogUtil.writeToFile(TAG,TAG + "---->onCreate,停止服务");
    }

    public static class RemoteBilder extends ITwoProcessAidlInterface.Stub {

        @Override
        public void doSomething() throws RemoteException {
        }

    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.w(TAG, "MDMOrderService被杀死了！");
            Intent remoteService = new Intent(PlayerMusicService.this,
                    MDMOrderService.class);
            TheTang.getSingleInstance().startService( remoteService );
            bindService(remoteService, connection, Context.BIND_AUTO_CREATE);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.w(TAG, "MDMOrderService连接成功！");
            //mBilder = (IBinder) ITwoProcessAidlInterface.Stub.asInterface( service );
        }
    };
}
