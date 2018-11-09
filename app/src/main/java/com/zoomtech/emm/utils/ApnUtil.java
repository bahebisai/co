package com.zoomtech.emm.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.zoomtech.emm.features.presenter.TheTang;

/**
 * Created by lenovo on 2018/1/8.
 */

public class ApnUtil {
    private static final String TAG = "ApnUtil";

    /**
     * 取得全部的APN列表
     */
    public static final Uri APN_URI = Uri.parse("content://telephony/carriers");

    /**
     * 取得当前设置的APN
     */
    public static final Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");

    /**
     * 取得current=1的APN
     **/
    public static void checkAPN() {
        // 检查当前连接的APN
        Cursor cr = TheTang.getSingleInstance().getContext().getContentResolver().query(APN_URI, null, null, null, null);
        while (cr != null && cr.moveToNext()) {
            String id = cr.getString(cr.getColumnIndex("_id"));
            String apn_id = cr.getString(cr.getColumnIndex("apn_id"));
            String apn = cr.getString(cr.getColumnIndex("apn"));
            String name = cr.getString(cr.getColumnIndex("name"));
            String type = cr.getString(cr.getColumnIndex("type")).toLowerCase();
            Log.d(TAG, "id= " + id + " ,apn= " + apn + "apn_id= " + apn_id + " type=" + type + " name=" + name);
        }
    }

    /**
     * 新增一个cmnet接入点
     *
     * @param values apn配置参数
     * @return 数据库id
     */
    public static int addAPN(ContentValues values) {
        int id = -1;
        Log.d(TAG, "添加一个新的apn");
        String NUMERIC = getSIMInfo();
        Log.d(TAG, "NUMERIC" + NUMERIC);
        if (NUMERIC == null) {
            return -1;
        }
        ContentResolver resolver = TheTang.getSingleInstance().getContext().getContentResolver();
        Cursor c = null;
        Uri newRow = resolver.insert(APN_URI, values);
        if (newRow != null) {
            c = resolver.query(newRow, null, null, null, null);
            int idIndex = c.getColumnIndex("_id");
            c.moveToFirst();
            id = c.getShort(idIndex);
        }
        if (c != null)
            c.close();
        return id;
    }

    private static String getSIMInfo() {
        TelephonyManager iPhoneManager = (TelephonyManager) TheTang.getSingleInstance().getContext().getSystemService(Context.TELEPHONY_SERVICE);
        return iPhoneManager.getSimOperator();
    }

    /**
     * 设置接入点
     *
     * @param id apn id
     */
    public static void SetAPN(int id) {
        ContentResolver resolver = TheTang.getSingleInstance().getContext().getContentResolver();
        ContentValues values = new ContentValues();
        values.put("apn_id", id);
        int update = resolver.update(PREFERRED_APN_URI, values, null, null);
        Log.w(TAG, "设置接入点--apn" + update);
    }

    /**
     * 删除apn
     *
     * @param name apn name
     */
    public static void deleteAPN(String name) {
        ContentResolver resolver = TheTang.getSingleInstance().getContext().getContentResolver();
        String where = "name=\"" + name + "\"";
        Log.w(TAG, "删除deleteAPN1--apn" + name);
        Cursor cursor = TheTang.getSingleInstance().getContext().getContentResolver().query(APN_URI, new String[]{"_id"}, " name = ?", new String[]{name}, null);
        Log.w(TAG, "cursor" + cursor);
        while (cursor != null && cursor.moveToNext()) {
            int idIndex = cursor.getColumnIndex("_id");
            //  cursor.moveToFirst();
            int id = cursor.getShort(idIndex);
            Log.d(TAG, "查询appid" + id);
            int delete = resolver.delete(APN_URI, " _id= ?", new String[]{id + ""});
            Log.d(TAG, delete + "删除了几条ss" + id);
        }
        cursor.close();
    }

    /**
     * 删除apn
     *
     * @param id db id
     */
    public static void deleteAPN(int id) {
        ContentResolver resolver = TheTang.getSingleInstance().getContext().getContentResolver();
        int delete = resolver.delete(APN_URI, "where _id= ?", new String[]{id + ""});
        Log.d(TAG, "删除了几条" + delete);
    }
}
