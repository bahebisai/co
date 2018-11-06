package com.xiaomi.emm.features.white;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.telephony.ITelephony;
import com.google.gson.Gson;
import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.features.db.DatabaseOperate;
import com.xiaomi.emm.features.impl.SendMessageManager;
import com.xiaomi.emm.model.MessageSendData;
import com.xiaomi.emm.model.PhoneLog;
import com.xiaomi.emm.model.TelephoyWhiteUser;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.features.manager.PreferencesManager;
import com.xiaomi.emm.features.presenter.TheTang;
import com.xiaomi.emm.utils.WifiHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/6/5.
 */

public class PhoneReceiver extends BroadcastReceiver {
    String TAG = "PhoneReceiver";
    static String telephoneNumber = "telephone_number";
    String callNumber = null;
    TelephonyManager mTelephonyManager;
    //private String incomingNumber_flage;
    Context mContext = TheTang.getSingleInstance().getContext();

    @Override
    public void onReceive(Context context, Intent intent) {
        mTelephonyManager = (TelephonyManager) context.getSystemService( Service.TELEPHONY_SERVICE );
        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();


        if (Intent.ACTION_NEW_OUTGOING_CALL.equals( intent.getAction() )) {
            //拨出
            callNumber = intent.getStringExtra( Intent.EXTRA_PHONE_NUMBER );

            if (!checkTelephoneNumeber( callNumber )) {
                //如果是黑名单，就设置电话为空，不进行拨号
                LogUtil.writeToFile( TAG, "拨出号码是：" + callNumber );

                //默认白名单关闭
                if (preferencesManager.getOtherData( Common.white_phone ) != null && "true".equals(preferencesManager.getOtherData(Common.white_phone))) {
                    setResultData(null);
                } else {
                    return;
                }

            } else {
                //把打电话动作(日志)存储起来，转给服务器
                ArrayList<String> list = new ArrayList<>();
            }
        } else {
            //设置一个呼入监听器
            mTelephonyManager.listen( listener_callIn, PhoneStateListener.LISTEN_CALL_STATE );
        }
    }

    PhoneStateListener listener_callIn = new PhoneStateListener() {

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            // state 当前状态 incomingNumber,
            super.onCallStateChanged( state, incomingNumber );
            switch (state) {
                //手机空闲了
                case TelephonyManager.CALL_STATE_IDLE:

                    //停止录音
                    mContext.stopService(new Intent(mContext, AudioRecorderService.class));

                    Log.w( TAG, "手机空闲了" + incomingNumber );
                    PreferencesManager preferencesManagers = PreferencesManager.getSingleInstance();

                    if (TextUtils.isEmpty( incomingNumber ) && !TextUtils.isEmpty( preferencesManagers.getComingNumberLog( "incomingNumber" ) )) {
                        Log.w( TAG, "---------手机空闲了--incomingNumber_flage==" + preferencesManagers.getComingNumberLog( "incomingNumber" ) );
                        String number = preferencesManagers.getComingNumberLog( "incomingNumber" ).split( "_" )[0];
                        String comeTime = preferencesManagers.getComingNumberLog( "incomingNumber" ).split( "_" )[1];
                        saveInCommingNumber( number, comeTime );
                    } else {
                        Log.w( TAG, incomingNumber + "incomingNumber手机空闲了--incomingNumber_flage==" + callNumber );
                    }
                    break;
                //电话被挂起(接听)
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.w( TAG, "电话被挂起" + incomingNumber );

                    Intent intent = new Intent(mContext, AudioRecorderService.class);
                    intent.putExtra(telephoneNumber, callNumber);
                    mContext.startService(intent);

                    break;
                //当电话呼入时
                case TelephonyManager.CALL_STATE_RINGING:
                    //TheTang.getSingleInstance().getContext().startService(new Intent(TheTang.getSingleInstance().getContext(), AudioRecorderService.class));
                    //在未登录和关闭电话白名单的情况下，电话白名单不起效
                    callNumber = incomingNumber;
                    PreferencesManager.getSingleInstance().setComingNumberLog( "incomingNumber", incomingNumber + "_" + System.currentTimeMillis() );
                    LogUtil.writeToFile( TAG, "来电号码是：" + incomingNumber );
                    Log.w( TAG, "来电号码是" + incomingNumber );
                    PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();

                    if (preferencesManager.getData( Common.alias ) == null ||
                            (preferencesManager.getOtherData( Common.white_phone ) != null && "false".equals(preferencesManager.getOtherData(Common.white_phone)))) {
                        return;
                    }

                    // 如果该号码属于黑名单
                    if (!checkTelephoneNumeber( incomingNumber )) {
                        LogUtil.writeToFile( TAG, "该号码属于黑名单：" + incomingNumber );
                        stopCall();
                    }
                    break;
            }
        }
    };

    private void saveInCommingNumber(String incomingNumber, String incomingTime) {

        String numberLog = PreferencesManager.getSingleInstance().getComingNumberLog( Common.ComingNumberLog );
        PhoneLog phoneLogs;

        ArrayList<PhoneLog.PhoneData> phoneBeanList;
        PhoneLog.PhoneData phoneLog = new PhoneLog.PhoneData();
        phoneLog.setCallNumber( incomingNumber );
        phoneLog.setCallTime( incomingTime );


        if (!TextUtils.isEmpty( numberLog )) {

            phoneLogs = new Gson().fromJson( numberLog, PhoneLog.class );
            phoneBeanList = phoneLogs.getPhoneBeanList();
            phoneBeanList.add( phoneLog );

        } else {

            phoneLogs = new PhoneLog();
            phoneLogs.setAlias( PreferencesManager.getSingleInstance().getData( Common.alias ) );
            phoneBeanList = new ArrayList<PhoneLog.PhoneData>();
            phoneBeanList.add( phoneLog );
            phoneLogs.setPhoneBeanList( phoneBeanList );
        }

        Log.w( TAG, new Gson().toJson( phoneLogs ) );
        String ssid = WifiHelper.getSSID();
        if (!TextUtils.isEmpty( ssid )) {
/*            ComingNumberLogImpl comingNumberLogImpl = new ComingNumberLogImpl( TheTang.getSingleInstance().getContext() );
            comingNumberLogImpl.sendComingNumberLog( new Gson().toJson( phoneLogs ) );*/

            //todo impl bai 33333333
            MessageSendData data = new MessageSendData(Common.coming_number_impl, new Gson().toJson(phoneLogs), false);
            SendMessageManager manager = new SendMessageManager();
            manager.setSendListener(new SendMessageManager.SendListener() {
                @Override
                public void onSuccess() {
                    PreferencesManager.getSingleInstance().clearComingNumberLog();//清除数据
                }

                @Override
                public void onFailure() {

                }

                @Override
                public void onError() {

                }
            });
            manager.sendMessage(data);
        } else {
            //存入sp有网就自动发给服务器
            PreferencesManager.getSingleInstance().setComingNumberLog( Common.ComingNumberLog, new Gson().toJson( phoneLogs ) );
        }
    }

    public void stopCall() {

        try {
            //方法一:需添加ITelephony.aidl文件，且包名须与系统的ITelephony类的路径保持一致。
            Method method = Class.forName( "android.os.ServiceManager" ).getMethod( "getService", String.class );
            // 获取远程TELEPHONY_SERVICE的IBinder对象的代理
            IBinder binder = (IBinder) method.invoke( null, new Object[]{"phone"} );
            // 将IBinder对象的代理转换为ITelephony对象
            ITelephony telephony = ITelephony.Stub.asInterface( binder );
            // 挂断电话
            telephony.endCall();
            //方法二
            /*Method m1 = tm.getClass().getDeclaredMethod("getITelephony");
            if (m1 != null) {
                m1.setAccessible(true);
                Object iTelephony = m1.invoke(tm);

                if (iTelephony != null) {
                    Method m3 = iTelephony.getClass().getDeclaredMethod("endCall");
                    if (m3 != null) {
                        m3.invoke(iTelephony);
                    }
                }
            }*/

        } catch (ClassNotFoundException e) {
            Log.d( TAG, "ClassNotFoundException" );
        } catch (InvocationTargetException e1) {
            Log.d( TAG, "InvocationTargetException" );
        } catch (NoSuchMethodException e) {
            Log.d( TAG, "NoSuchMethodException" );
        } catch (IllegalAccessException e) {
            Log.d( TAG, "IllegalAccessException" );
        } catch (RemoteException e) {
            Log.d( TAG, "RemoteException" );
        }
    }

    /**
     * 检测白名单是否保存该号码
     *
     * @param number
     * @return true/false
     */
    public boolean checkTelephoneNumeber(String number) {
        List<TelephoyWhiteUser> telephoyWhiteUsers = new ArrayList<>();
        telephoyWhiteUsers = DatabaseOperate.getSingleInstance().queryTelephonyWhite();
        for (TelephoyWhiteUser telephoyWhiteUser : telephoyWhiteUsers) {
            if (telephoyWhiteUser.getTelephonyNumber().replace( "-", "" ).contains( number )) {
                return true;
            }
        }
        return false;
    }


}
