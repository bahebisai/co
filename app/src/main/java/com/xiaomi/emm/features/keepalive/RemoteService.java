package com.xiaomi.emm.features.keepalive;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.xiaomi.emm.ITwoProcessAidlInterface;
import com.xiaomi.emm.features.excute.MDMOrderService;
import com.xiaomi.emm.features.presenter.TheTang;

/**
 * Created by Administrator on 2018/2/8.
 */

public class RemoteService extends Service {

    private final static String TAG = "RemoteService";

    private IBinder mBilder = new RemoteBilder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBilder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand( intent, flags, startId );
        Log.w(TAG, "onStartCommand！");
        //绑定远程服务
        bindService( new Intent( this, MDMOrderService.class ),
                connection, Context.BIND_AUTO_CREATE );
        return START_REDELIVER_INTENT;
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
            Intent remoteService = new Intent(RemoteService.this,
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