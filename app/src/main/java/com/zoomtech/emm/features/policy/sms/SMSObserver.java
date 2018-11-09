package com.zoomtech.emm.features.policy.sms;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Telephony;
import android.util.Log;

import com.zoomtech.emm.utils.TimeUtils;


public class SMSObserver extends ContentObserver {Context mContext;
    long mLastQueryId = -1;

    Handler mHandler;

    public SMSObserver(Context context, Handler handler) {
        super(handler);
        mContext = context;
        mHandler = handler;
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
//        setSmsCode();
        long id = -1;
        String uriString = uri.toString();
        String index = uriString.substring(uriString.lastIndexOf("/")+1);
        if (index != null && index.length() > 0 && TimeUtils.isNumeric(index)) {
            id = Long.parseLong(index); //replace  ContentUris.parseId(uri) for NumberFormatException, eg.content://sms/inbox
        }
/*        try {
            id = ContentUris.parseId(uri); //replaced for NumberFormatException, eg.content://sms/inbox
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }*/
        if (id != -1 && id != mLastQueryId && isFinished(uri)) {
//            Log.d("baii", "selfChange " + selfChange + ", uri: " + uri);
//            getSmsMessage(uri);
            sendMessage(uri);
            mLastQueryId = id;
        }
    }

    private void getSmsMessage(Uri uri) {
        Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
//                String body = cursor.getString(cursor.getColumnIndex("body"));
//                Log.d("baii", "uri body " + body);
            }
        }
        cursor.close();
    }

    /**
     *    type      短信类型     integer 1：inbox  2：sent 3：draft56  4：outbox  5：failed  6：queued
     *    监听sms数据库变化，发送一条短信要经过type的6,4,2三个状态变化
     *    只有发送或接收才通知上传
     * @param uri
     * @return
     */
    private boolean isFinished(Uri uri) {
        Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int type1 = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.TYPE));
//                Log.d("baii", "getSmsInfo type1 " + type1);
                if (type1 == 1 || type1 == 2) {
                    return true;
                } else {
                    return false;
                }
            }
            cursor.close();
        }
        return false;
    }

    private void sendMessage(Uri uri) {
        Message message = new Message();
        message.what = SmsManager.SMS_CHANGED;
        Bundle bundle = new Bundle();
        bundle.putString("uri", uri.toString());
//        Log.d("baii", "uri to string " + uri.toString());
        message.setData(bundle);
        mHandler.sendMessage(message);
    }


}
