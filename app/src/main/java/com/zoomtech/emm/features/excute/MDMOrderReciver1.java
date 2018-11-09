package com.zoomtech.emm.features.excute;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.zoomtech.emm.utils.LogUtil;
import com.zoomtech.emm.features.presenter.TheTang;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Administrator on 2017/12/11.
 */

public class MDMOrderReciver1 {

    public final static String TAG = "MDMOrderReciver1";
    static Map<String, String> msgIdMap = new HashMap<>();

    //单例
    private volatile static MDMOrderReciver1 mMDMOrderReciver1;

    private MDMOrderReciver1() {
    }

    /**
     * 单例
     *
     * @return
     */
    public static MDMOrderReciver1 getSingleInstance() {
        if (null == mMDMOrderReciver1) {
            synchronized (MDMOrderReciver1.class) {
                if (null == mMDMOrderReciver1) {
                    mMDMOrderReciver1 = new MDMOrderReciver1();
                }
            }
        }
        return mMDMOrderReciver1;
    }

    public void handleMessage(Bundle bundle) {
        synchronized (mMDMOrderReciver1) {
            String extra = bundle.getString( "Push" );

            MDMOrderMessageBackImpl mMDMOrderMessageBackImpl = new MDMOrderMessageBackImpl();
            if ( mMDMOrderMessageBackImpl.feedbackOrderMessage( TheTang.getSingleInstance().getContext(), extra ) ) {
                MDMOrderExcuting mdmOrderExcuting = new MDMOrderExcuting();
                mdmOrderExcuting.processOrderMessage(TheTang.getSingleInstance().getContext(), extra);
            }
        }
    }

    public boolean excuteMessage(String sendId) {

        if ("-2".equals( sendId )) {
            return false;
        }

        if (whetherIsSameOrder( sendId )) {
            Log.w( TAG, "重复消息：MSGID = " + sendId );
            LogUtil.writeToFile( TAG, "重复消息：MSGID = " + sendId );
            return true;
        }
        return false;
    }

    /**
     * 判断是否为相同命令
     *
     * @param newMsgId
     * @return
     */
    private boolean whetherIsSameOrder(String newMsgId) {

        Iterator<Map.Entry<String, String>> iterator = msgIdMap.entrySet().iterator();
        // try {

        if (iterator.hasNext()) {

            Map.Entry<String, String> entry = iterator.next();

            Log.e( TAG, "iterator == " + entry.toString() );

            if (!TextUtils.isEmpty( entry.getKey() ) && entry.getKey().equals( newMsgId )) {
                if (System.currentTimeMillis() - Long.valueOf( entry.getValue() ) >= 3600000) {
                    msgIdMap.put( newMsgId, System.currentTimeMillis() + "" );
                    return false;
                } else {
                    msgIdMap.put( newMsgId, System.currentTimeMillis() + "" );
                    return true;
                }
            } else {
                if (System.currentTimeMillis() - Long.valueOf( entry.getValue() ) >= 3600000) {
                    msgIdMap.remove( entry.getKey() );
                }
            }
        }
        msgIdMap.put( newMsgId, System.currentTimeMillis() + "" );
        return false;
    }
}
