package com.zoomtech.emm.features.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.zoomtech.emm.definition.OrderConfig;
import com.zoomtech.emm.model.APPInfo;
import com.zoomtech.emm.model.CompleteMessageData;
import com.zoomtech.emm.model.DownLoadEntity;
import com.zoomtech.emm.model.MessageInfo;
import com.zoomtech.emm.model.MessageSendData;
import com.zoomtech.emm.model.SensitiveStrategyInfo;
import com.zoomtech.emm.model.StrategeInfo;
import com.zoomtech.emm.model.TelephoyWhiteUser;
import com.zoomtech.emm.utils.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2017/6/13.
 */

public class DatabaseOperate {
    private static final String TAG = "DatabaseOperate";

    static SQLiteDatabase mSQLiteDatabase;
    private static DatabaseOperate mDatabaseOperate = null;
    private AtomicInteger mOpenCounter = new AtomicInteger();
    private Context mContext;

    /***************************电话白名单*******************************/

    private String insert_white_sql = "insert into " + DataBaseHelper.white_table
            + " (" + DataBaseHelper.white_name + "," + DataBaseHelper.white_id + "," + DataBaseHelper.white_address + ","
            + DataBaseHelper.white_loginName + ","
            + DataBaseHelper.white_shortPhoneNum + ","
            + DataBaseHelper.white_phone + ") values (?,?,?,?,?,?)";


    private String search_white_sql = "select * from " + DataBaseHelper.white_table + " where " + DataBaseHelper.white_name + " like ? " +
            "or " + DataBaseHelper.white_phone + " like ? or " + DataBaseHelper.white_address + " like ? or " + DataBaseHelper.white_shortPhoneNum + " like ?";

    private String query_single_white_sql = "select * from " + DataBaseHelper.white_table + " where " + DataBaseHelper.white_id + " = ? ";

    private String is_white_sql = "select * from " + DataBaseHelper.white_table + " where " + DataBaseHelper.white_id + " = ? ";
    private String query_white_sql = "select * from " + DataBaseHelper.white_table;

    private String delete_white_sql = "delete from " + DataBaseHelper.white_table + " where " + DataBaseHelper.white_id + " = ?";

    private String update_white_sql = "update " + DataBaseHelper.white_table + " set "
            + DataBaseHelper.white_phone + " =? , "
            + DataBaseHelper.white_address + " =? , "
            + DataBaseHelper.white_shortPhoneNum + " =? , "
            + DataBaseHelper.white_loginName + " =? , "
            + DataBaseHelper.white_name + " =? where " +
            DataBaseHelper.white_id + " = ?";

    /***************************应用安装*******************************/

    private String insert_app_sql = "insert into " + DataBaseHelper.installApp_table
            + " (" + DataBaseHelper.app_id + "," + DataBaseHelper.app_name + ","
            + DataBaseHelper.package_name + ") values (?,?,?)";

    private String query_app_sql = "select * from " + DataBaseHelper.installApp_table + " where " + DataBaseHelper.app_id + " like ? or "
            + DataBaseHelper.app_name + " like ? or " + DataBaseHelper.package_name + " like ?";

    private String query_all_app_sql = "select * from " + DataBaseHelper.installApp_table;

    private String delete_app_sql = "delete from " + DataBaseHelper.installApp_table + " where " + DataBaseHelper.app_id
            + " like ? or " + DataBaseHelper.app_name + " like ? or " + DataBaseHelper.package_name + " like ?";

    private String update_app_sql = "update " + DataBaseHelper.installApp_table + " set " + DataBaseHelper.app_id
            + " = ? , " + DataBaseHelper.app_name + " = ? where " + DataBaseHelper.package_name + " = ?";

    /***************************文件下载*******************************/

    private String downfile_insert_sql = "insert into " + DataBaseHelper.downLoadFile_table
            + " (" + DataBaseHelper.downLoadFile_id + "," + DataBaseHelper.downLoadFile_sendId + "," + DataBaseHelper.downLoadFile_code + ","
            + DataBaseHelper.downLoadFile_start + "," + DataBaseHelper.downLoadFile_downed + "," + DataBaseHelper.downLoadFile_total + ","
            + DataBaseHelper.downLoadFile_saveName + "," + DataBaseHelper.downLoadFile_packageName + "," + DataBaseHelper.downLoadFile_internet + ","
            + DataBaseHelper.downLoadFile_uninstall + ","+ DataBaseHelper.downLoadFile_type + ") values (?,?,?,?,?,?,?,?,?,?,?)";

    private String downfile_query_sql = "select * from " + DataBaseHelper.downLoadFile_table + " where " + DataBaseHelper.downLoadFile_id + " = ?";

    private String downfile_query_sql_by_sendId = "select * from " + DataBaseHelper.downLoadFile_table + " where " + DataBaseHelper.downLoadFile_sendId + " = ?";

    private String downfile_query_sql_by_packagename = "select * from " + DataBaseHelper.downLoadFile_table + " where " + DataBaseHelper.downLoadFile_packageName + " = ?";

    private String all_downfile_query_sql = "select * from " + DataBaseHelper.downLoadFile_table;

    private String update_downfile_sql = "update " + DataBaseHelper.downLoadFile_table + " set " + DataBaseHelper.downLoadFile_downed
            + " =? , " + DataBaseHelper.downLoadFile_saveName + " =? , " + DataBaseHelper.downLoadFile_total + " =? where " + DataBaseHelper.downLoadFile_id + " =?";

    private String delete_downfile_sql = "delete from " + DataBaseHelper.downLoadFile_table + " where " + DataBaseHelper.downLoadFile_id + " = ?";

    /***************************通知*******************************/
    private String add_message_sql = "insert into " + DataBaseHelper.message_table + " (" + DataBaseHelper.message_icon + ","
            + DataBaseHelper.message_id + "," + DataBaseHelper.message_from + "," + DataBaseHelper.message_time + ","
            + DataBaseHelper.message_about + ") values (?,?,?,?,?)";

    private String query_message_sql = "select * from " + DataBaseHelper.message_table /* + " by " + name + " asc"*/;

    private String delete_message_sql = "delete from " + DataBaseHelper.message_table + " where " + DataBaseHelper.message_time + " = ?";

    private String update_message_sql = "update " + DataBaseHelper.message_table + " set " + DataBaseHelper.message_icon
            + " =? where " + DataBaseHelper.message_time + " = ?";

    /***************************文件*******************************/
    private String add_file_sql = "insert into " + DataBaseHelper.file_table + " (" + DataBaseHelper.file_id + ","
            + DataBaseHelper.file_name + ") values (?,?)";

    private String query_file_sql = "select * from " + DataBaseHelper.file_table + " where " + DataBaseHelper.file_id + " =?";

    private String delete_file_sql = "delete from " + DataBaseHelper.file_table + " where " + DataBaseHelper.file_id + " =?";

    /***************************策略*******************************/
    private String add_stratege_sql = "insert into " + DataBaseHelper.stratege_table + " (" + DataBaseHelper.STRATEGY_CODE + ","
            + DataBaseHelper.stratege_name + "," + DataBaseHelper.stratege_time + ") values (?,?,?)";

    private String query_stratege_sql = "select * from " + DataBaseHelper.stratege_table + " where " + DataBaseHelper.STRATEGY_CODE + " =?";
    private String query_strategy_sql_by_name = "select * from " + DataBaseHelper.stratege_table + " where " + DataBaseHelper.stratege_name + " =?";

    private String query_all_stratege_sql = "select * from " + DataBaseHelper.stratege_table;

    private String delete_stratege_sql = "delete from " + DataBaseHelper.stratege_table;

    private String delete_simple_stratege_sql = "delete from " + DataBaseHelper.stratege_table + " where " + DataBaseHelper.STRATEGY_CODE + " =?";

    private String delete_simple_strategy_sql_by_name = "delete from " + DataBaseHelper.stratege_table +
            " where " + DataBaseHelper.STRATEGY_CODE + " =?" + " and " + DataBaseHelper.stratege_name + " =?";

    private String update_stratege_sql = "update " + DataBaseHelper.stratege_table + " set " + DataBaseHelper.stratege_name
            + " =? , " + DataBaseHelper.stratege_time + " =? where " + DataBaseHelper.STRATEGY_CODE + " = ?";

    /***************************应用白名单*******************************/
    private String add_app_compliance_sql = "insert into " + DataBaseHelper.app_table + " (" + DataBaseHelper.app_compliance_name + ") values (?)";

    private String query_all_app_compliance_sql = "select * from " + DataBaseHelper.app_table;

    private String delete_all_app_compliance_sql = "delete from " + DataBaseHelper.app_table;

    private String delete_app_compliance_sql = "delete from " + DataBaseHelper.app_table + " where " + DataBaseHelper.app_compliance_name + " =?";

    /***************************应用违规名单*******************************/
    private String add_app_deny_sql = "insert into " + DataBaseHelper.app_deny + " (" + DataBaseHelper.app_deny_name + "," + DataBaseHelper.app_deny_type
            + "," + DataBaseHelper.app_deny_package + ") values (?,?,?)";

    private String query_all_app_deny_sql = "select * from " + DataBaseHelper.app_deny;

    private String delete_all_app_deny_sql = "delete from " + DataBaseHelper.app_deny;

    private String update_app_deny = "update " + DataBaseHelper.app_deny + " set " + DataBaseHelper.app_deny_type + " =? where " + DataBaseHelper.app_deny_package + " =?";

    private String query_app_deny_from_type = "select * from " + DataBaseHelper.app_deny + " where " + DataBaseHelper.app_deny_type + " =?";

    private String query_app_deny_from_name = "select * from " + DataBaseHelper.app_deny + " where " + DataBaseHelper.app_deny_package + " =?";


    /**********************************返回结果失败存储临时表************************************/
    // id 是对应标志   data是存储的内容是以json的格式
    private String add_backResult_sql = "insert into " + DataBaseHelper.backResult_table + " (" + DataBaseHelper.backResult_type + ","
            + DataBaseHelper.backResult_data +  ") values (?,?)";

    private String query_backResult_sql = "select * from " + DataBaseHelper.backResult_table + " where " + DataBaseHelper.backResult_type + " =? ";

    private String queryAll_backResult_sql = "select * from " + DataBaseHelper.backResult_table;

    private String delete_all_backResult_sql = "delete from " + DataBaseHelper.backResult_table;

    private String delete_id_backResult_sql = "delete from " + DataBaseHelper.backResult_table + " where " + " _id =? ";

    /***************************命令执行返回*******************************/

    private String add_completeResult_sql = "insert into " + DataBaseHelper.complete_table + " (" + DataBaseHelper.complete_type + "," +
            DataBaseHelper.complete_result + "," + DataBaseHelper.complete_time + "," + DataBaseHelper.complete_id + ") values (?,?,?,?)";

    private String query_completeResult_sql = "select * from " + DataBaseHelper.complete_table + " where " + DataBaseHelper.complete_type +
            " =? and " + DataBaseHelper.complete_id + " =? ";

    private String query_completeResultType_sql = "select * from " + DataBaseHelper.complete_table + " where " + DataBaseHelper.complete_type + " =? ";

    private String queryAll_completeResult_sql = "select * from " + DataBaseHelper.complete_table;

    private String update_completeResult_sql = "update " + DataBaseHelper.complete_table + " set " + DataBaseHelper.complete_result +
            " =? where " + DataBaseHelper.complete_type + " =? and " + DataBaseHelper.complete_id + " = ?";

    private String update_completeTime_sql = "update " + DataBaseHelper.complete_table + " set " + DataBaseHelper.complete_time +
            " =? where " + DataBaseHelper.complete_type + " =? and " + DataBaseHelper.complete_id + " = ?";

    private String update_completeResultAndTime_sql = "update " + DataBaseHelper.complete_table + " set " + DataBaseHelper.complete_result + " =? , "
            + DataBaseHelper.complete_time + " =? where " + DataBaseHelper.complete_type + " =? and " + DataBaseHelper.complete_id + " = ?";

    private String  delete_completeResult_sql = "delete from " + DataBaseHelper.complete_table + " where " + DataBaseHelper.complete_type +
            " =? and " + DataBaseHelper.complete_id + " = ?";

    /***************************敏感词*********************************************/
    private String add_sensitiveWord_sql = "insert into " + DataBaseHelper.SENSITIVE_WORD_TABLE + " (" + DataBaseHelper.SW_STRATEGY_ID + ","
            + DataBaseHelper.WORDS  + "," + DataBaseHelper.NAME + ") values (?,?,?)";

    private String query_sensitiveWord_sql = "select * from " + DataBaseHelper.SENSITIVE_WORD_TABLE + " where " + DataBaseHelper.SW_STRATEGY_ID + " =?";
    private String query_sensitiveWord_sql_by_name = "select * from " + DataBaseHelper.SENSITIVE_WORD_TABLE + " where " + DataBaseHelper.NAME + " =?";
    private String queryAll_sensitiveWord_sql = "select * from " + DataBaseHelper.SENSITIVE_WORD_TABLE;

    private String delete_sensitiveWord_sql = "delete from " + DataBaseHelper.SENSITIVE_WORD_TABLE + " where " + DataBaseHelper.SW_STRATEGY_ID + " =?";
    private String update_sensitiveWord_sql = "update " + DataBaseHelper.SENSITIVE_WORD_TABLE + " set " + DataBaseHelper.WORDS + " =? where " + DataBaseHelper.SW_STRATEGY_ID + " =?";

    /******************************************************/
    /**
     * 防止被close
     */
    private synchronized void openSQLiteDataBase() {
        if (mOpenCounter.incrementAndGet() == 1 || mSQLiteDatabase == null) {
            mSQLiteDatabase = DataBaseHelper.getSingleInstance( mContext ).getWritableDatabase();
        }
    }

    /**
     * 防止被close
     */
    private synchronized void closeSQLiteDatabase() {
        if (mOpenCounter.decrementAndGet() == 0) {
            mSQLiteDatabase.close();
        }
    }

    private DatabaseOperate() {

    }

    //单例
    public static DatabaseOperate getSingleInstance() {
        if (null == mDatabaseOperate) {
            synchronized (DatabaseOperate.class) {
                if (null == mDatabaseOperate) {
                    mDatabaseOperate = new DatabaseOperate();
                }
            }
        }
        return mDatabaseOperate;
    }

    public void init(Context context) {
        mContext = context;
        LogUtil.writeToFile( TAG, "DatabaseOperate init!" );
    }

    /************************电话白名单相关数据操作*****************************/
    public synchronized boolean isTelephonyWhite(String telephonyUser) {

        boolean result = false;
        Cursor cursor = null;
        openSQLiteDataBase();

        mSQLiteDatabase.beginTransaction();
        try {
            cursor = mSQLiteDatabase.rawQuery( is_white_sql, new String[]{telephonyUser} );
            if (cursor != null) {
                result = cursor.getCount() > 0;
            }
            LogUtil.writeToFile( TAG, telephonyUser + " is Telephony White : " + result );
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "isTelephonyWhite" + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }

        if (cursor != null) {

            cursor.close();
        }
        closeSQLiteDatabase();
        return result;
    }

    /**
     * query single telephony white list
     *
     * @param telephonyId
     * @return
     * @desc
     */
    public synchronized TelephoyWhiteUser querySingleTelephonyWhite(String telephonyId) {

        TelephoyWhiteUser telephoyWhiteUser = new TelephoyWhiteUser();
        Cursor cursor = null;
        openSQLiteDataBase();

        mSQLiteDatabase.beginTransaction();
        try {
            cursor = mSQLiteDatabase.rawQuery( query_single_white_sql, new String[]{telephonyId/*, telephonyUser, telephonyUser*/} );
            if (cursor != null && cursor.getCount() > 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    telephoyWhiteUser.setUserName( cursor.getString( cursor.getColumnIndex( DataBaseHelper.white_name ) ) );
                    telephoyWhiteUser.setUserId( cursor.getString( cursor.getColumnIndex( DataBaseHelper.white_id ) ) );
                    telephoyWhiteUser.setUserAddress( cursor.getString( cursor.getColumnIndex( DataBaseHelper.white_address ) ) );
                    telephoyWhiteUser.setTelephonyNumber( cursor.getString( cursor.getColumnIndex( DataBaseHelper.white_phone ) ) );
                    telephoyWhiteUser.setShortPhoneNum(cursor.getString(cursor.getColumnIndex(DataBaseHelper.white_shortPhoneNum)));
                    telephoyWhiteUser.setLoginName(cursor.getString(cursor.getColumnIndex(DataBaseHelper.white_loginName)));
                }
                mSQLiteDatabase.setTransactionSuccessful();
            }
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "isTelephonyWhite" + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        //}
        if (cursor != null) {

            cursor.close();
        }
        closeSQLiteDatabase();
        return telephoyWhiteUser;
    }


    public synchronized boolean cleanTelephonyWhite() {
        String sql = "delete from " + DataBaseHelper.white_table;  //清空数据
        openSQLiteDataBase();

        mSQLiteDatabase.beginTransaction();
        try {
            mSQLiteDatabase.execSQL(sql);
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile(TAG, "cleanTelephonyWhite" + e.toString());
            return false;
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        closeSQLiteDatabase();
        return true;
    }

    /**
     * 查询是否在白名单
     */
    public synchronized List<TelephoyWhiteUser> searchTelephonyWhite(String searchText) {

        if (TextUtils.isEmpty( searchText )) {
            return null;
        }

        List<TelephoyWhiteUser> telephonys = new ArrayList<TelephoyWhiteUser>();
        Cursor cursor = null;
        openSQLiteDataBase();

        mSQLiteDatabase.beginTransaction();
        try {
            cursor = mSQLiteDatabase.rawQuery(search_white_sql, new String[]{"%" + searchText + "%", "%" + searchText + "%", "%" + searchText + "%", "%" + searchText + "%"});
            if (cursor != null && cursor.getCount() > 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    TelephoyWhiteUser telephoyWhiteUser = new TelephoyWhiteUser();
                    telephoyWhiteUser.setUserName( cursor.getString( cursor.getColumnIndex( DataBaseHelper.white_name ) ) );
                    telephoyWhiteUser.setUserId( cursor.getString( cursor.getColumnIndex( DataBaseHelper.white_id ) ) );
                    telephoyWhiteUser.setUserAddress( cursor.getString( cursor.getColumnIndex( DataBaseHelper.white_address ) ) );
                    telephoyWhiteUser.setTelephonyNumber( cursor.getString( cursor.getColumnIndex( DataBaseHelper.white_phone ) ) );
                    telephoyWhiteUser.setLoginName(cursor.getString(cursor.getColumnIndex(DataBaseHelper.white_loginName)));
                    telephoyWhiteUser.setShortPhoneNum(cursor.getString(cursor.getColumnIndex(DataBaseHelper.white_shortPhoneNum)));
                    telephonys.add( telephoyWhiteUser );
                }
            }
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "searchTelephonyWhite" + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        if (cursor != null) {

            cursor.close();
        }
        closeSQLiteDatabase();
        LogUtil.writeToFile( TAG, "search Telephony White result : " + telephonys.toString() );
        return telephonys;
    }

    /**
     * 查询全部白名单
     */
    public synchronized List<TelephoyWhiteUser> queryTelephonyWhite() {
        List<TelephoyWhiteUser> telephonys = new ArrayList<TelephoyWhiteUser>();
        Cursor cursor = null;
        openSQLiteDataBase();

        mSQLiteDatabase.beginTransaction();
        try {
            cursor = mSQLiteDatabase.rawQuery( query_white_sql, null );
            //cursor = mSQLiteDatabase.rawQuery(DataBaseHelper.WHITE_TABLE,null);
            if (cursor != null && cursor.getCount() > 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    TelephoyWhiteUser telephoyWhiteUser = new TelephoyWhiteUser();
                    telephoyWhiteUser.setUserName( cursor.getString( cursor.getColumnIndex( DataBaseHelper.white_name ) ) );
                    telephoyWhiteUser.setUserId( cursor.getString( cursor.getColumnIndex( DataBaseHelper.white_id ) ) );
                    telephoyWhiteUser.setUserAddress(cursor.getString(cursor.getColumnIndex(DataBaseHelper.white_address)));
                    telephoyWhiteUser.setTelephonyNumber( cursor.getString( cursor.getColumnIndex( DataBaseHelper.white_phone ) ) );
                    telephoyWhiteUser.setLoginName(cursor.getString(cursor.getColumnIndex(DataBaseHelper.white_loginName)));
                    telephoyWhiteUser.setShortPhoneNum(cursor.getString(cursor.getColumnIndex(DataBaseHelper.white_shortPhoneNum)));
                    telephonys.add( telephoyWhiteUser );
                }
            }
            mSQLiteDatabase.setTransactionSuccessful();

        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "queryTelephonyWhite" + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        if (cursor != null) {

            cursor.close();
        }
        closeSQLiteDatabase();
        LogUtil.writeToFile( TAG, "query all Telephony White result!" );
        return telephonys;
    }

    /**
     * 添加电话白名单
     */
    public synchronized void addTelephonyWhiteList(List<TelephoyWhiteUser> telephonys) {
        openSQLiteDataBase();

        try {
            mSQLiteDatabase.beginTransaction();
            for (TelephoyWhiteUser telephony : telephonys) {
                if (!isTelephonyWhite( telephony.getUserId() )) {
                    mSQLiteDatabase.execSQL(
                            insert_white_sql,
                            new String[]{
                                    telephony.getUserName(),
                                    telephony.getUserId(),
                                    telephony.getUserAddress(),
                                    telephony.getLoginName(),
                                    telephony.getShortPhoneNum(),
                                    telephony.getTelephonyNumber(),
                            });
                    LogUtil.writeToFile( TAG, "success add a Telephony White result : " + telephony.getUserId() );
                    Log.w(TAG, "success add a Telephony White result : " + telephony.getShortPhoneNum());
                } else {
                    updateTelephonyWhiteList( telephony );
                    Log.w(TAG, "success add telephony : " + telephony.getShortPhoneNum());
                }
            }
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.writeToFile( TAG, "addTelephonyWhiteList" + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        //  }
        closeSQLiteDatabase();
    }

    /**
     * 删除电话白名单
     */
    public synchronized void deleteTelephonyWhiteList(List<TelephoyWhiteUser> telephonys) {
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            for (TelephoyWhiteUser telephony : telephonys) {
                String str = telephony.getUserId();
                if (isTelephonyWhite( str )) {

                    mSQLiteDatabase.execSQL( delete_white_sql, new Object[]{str} );
                }
            }
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.writeToFile( TAG, "deleteTelephonyWhiteList" + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        closeSQLiteDatabase();
    }

    /**
     * 更新白名单
     *
     * @param telephony
     */

    public synchronized void updateTelephonyWhiteList(TelephoyWhiteUser telephony) {
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            mSQLiteDatabase.execSQL( update_white_sql, new String[]{telephony.getTelephonyNumber(),
                            telephony.getUserAddress(),
                            telephony.getShortPhoneNum(),
                            telephony.getLoginName(),
                            telephony.getUserName(),
                            telephony.getUserId(),});
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.writeToFile( TAG, "updateTelephonyWhiteList" + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        closeSQLiteDatabase();
    }


    /************************APP安装相关数据操作*****************************/
    /**
     * 查询已安装应用信息
     */
    public synchronized APPInfo queryAppInfo(String app_id) {
        Cursor cursor = null;
        APPInfo appInfo = null;
        openSQLiteDataBase();

        mSQLiteDatabase.beginTransaction();
        try {
            cursor = mSQLiteDatabase.rawQuery( query_app_sql, new String[]{"%" + app_id + "%", "%" + app_id + "%", "%" + app_id + "%"} );
            if (cursor != null && cursor.getCount() > 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    appInfo = new APPInfo();
                    appInfo.setAppId( cursor.getString( cursor.getColumnIndex( DataBaseHelper.app_id ) ) );
                    appInfo.setPackageName( cursor.getString( cursor.getColumnIndex( DataBaseHelper.package_name ) ) );
                    appInfo.setAppName( cursor.getString( cursor.getColumnIndex( DataBaseHelper.app_name ) ) );

                }
            }
            mSQLiteDatabase.setTransactionSuccessful();

        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "queryAppInfo" + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        if (cursor != null) {

            cursor.close();
        }
        closeSQLiteDatabase();
        //    }
        return appInfo;
    }

    /**
     * 获取全部安装应用信息
     *
     * @return
     */
    public synchronized List<APPInfo> queryInstallAppInfo() {
        List<APPInfo> list = new ArrayList<>();
        Cursor cursor = null;
        openSQLiteDataBase();

        try {
            APPInfo appInfo = null;
            mSQLiteDatabase.beginTransaction();
            cursor = mSQLiteDatabase.rawQuery( query_all_app_sql, null );
            if (cursor != null && cursor.getCount() > 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    appInfo = new APPInfo();
                    appInfo.setAppId( cursor.getString( cursor.getColumnIndex( DataBaseHelper.app_id ) ) );
                    appInfo.setAppName( cursor.getString( cursor.getColumnIndex( DataBaseHelper.app_name ) ) );
                    appInfo.setPackageName( cursor.getString( cursor.getColumnIndex( DataBaseHelper.package_name ) ) );
                    list.add( appInfo );
                }
            }
            mSQLiteDatabase.setTransactionSuccessful();

        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "queryInstallAppInfo" + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        //      }
        if (cursor != null) {

            cursor.close();
        }
        closeSQLiteDatabase();
        return list;
    }

    /**
     * 添加已安装应用信息
     */
    public synchronized void addInstallAppInfo(List<APPInfo> installAPPInfos) {
        openSQLiteDataBase();

        try {
            mSQLiteDatabase.beginTransaction();
            for (APPInfo installAPPInfo : installAPPInfos) {

                if (queryAppInfo( installAPPInfo.getPackageName() ) == null) {

                    ContentValues contentValues = new ContentValues();
                    contentValues.put( DataBaseHelper.app_id, installAPPInfo.getAppId() );
                    contentValues.put( DataBaseHelper.app_name, installAPPInfo.getAppName() );
                    contentValues.put( DataBaseHelper.package_name, installAPPInfo.getPackageName() );
                    mSQLiteDatabase.insert( DataBaseHelper.installApp_table, null, contentValues );
                    LogUtil.writeToFile( TAG, "add success app " + installAPPInfo.getAppName() + " app_id " + installAPPInfo.getAppId() );
                    Log.w( TAG, "add success app " + installAPPInfo.getAppName() + " PackageName " + installAPPInfo.getPackageName() );
                } else {
                    updateInstallAppInfo( installAPPInfo );
                }
            }
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "add App Info" + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
            LogUtil.writeToFile( TAG, "add success app endTransaction！" );
        }
        // }
        closeSQLiteDatabase();
    }

    /**
     * 删除已安装应用信息
     */
    public synchronized void deleteInstallAppInfo(String app_id) {
        openSQLiteDataBase();

        try {
            mSQLiteDatabase.beginTransaction();
            if (queryAppInfo( app_id ) != null) {
                mSQLiteDatabase.execSQL( delete_app_sql, new String[]{"%" + app_id + "%", "%" + app_id + "%", "%" + app_id + "%"} );
            }
            mSQLiteDatabase.setTransactionSuccessful();
            LogUtil.writeToFile( TAG, "deleteInstallAppInfo sucess!" );
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "deleteInstallAppInfo" + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        //   }
        closeSQLiteDatabase();
    }

    /**
     * 更新已安装App信息
     *
     * @param installAPPInfo
     */
    private synchronized void updateInstallAppInfo(APPInfo installAPPInfo) {
        openSQLiteDataBase();

        try {
            mSQLiteDatabase.beginTransaction();
            mSQLiteDatabase.execSQL( update_app_sql, new String[]{installAPPInfo.getAppId(), /*installAPPInfo.getVersion(),installAPPInfo.getSize(),*/
                    installAPPInfo.getAppName(), installAPPInfo.getPackageName()} );
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "updateInstallAppInfo" + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        //   }
        closeSQLiteDatabase();
    }
    /************************下载相关数据操作*****************************/
    /**
     * 添加下载文件数据
     *
     * @param app_id
     * @param start
     * @param downed
     * @param total
     * @param packageName .
     * @return
     */
    public synchronized DownLoadEntity insertDownLoadFile(String app_id, String sendId, String code, int start, int downed, int total,
                               String saveName, String packageName, String type, String internet, String uninstall) {

        DownLoadEntity entity = null;

        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            //防止同时下发相同文件的下载命令，保证在下载列表中应用的唯一性
            if (queryDownLoadFile( app_id ) == null) {
                mSQLiteDatabase.execSQL( downfile_insert_sql, new String[]{app_id, sendId, code, String.valueOf( start ), String.valueOf( downed ),

                        String.valueOf( total ), saveName, packageName, internet, uninstall, type} );
            }
            entity = queryDownLoadFile( app_id );

            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "insertDownLoadFile" + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        closeSQLiteDatabase();
        return entity;
    }

    /**
     * 通过app_id获取下载文件信息
     *
     * @param app_id
     * @return
     */
    public synchronized DownLoadEntity queryDownLoadFile(String app_id) {
        DownLoadEntity downLoadEntity = null;
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();

            Cursor cursor = mSQLiteDatabase.rawQuery( downfile_query_sql, new String[]{app_id} );
            if (cursor != null && cursor.getCount() > 0) {
                if (cursor.moveToNext()) {
                    downLoadEntity = new DownLoadEntity();
                    downLoadEntity.app_id = cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_id ) );
                    downLoadEntity.sendId = cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_sendId ) );
                    downLoadEntity.code = cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_code ) );
                    downLoadEntity.start = Integer.parseInt( cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_start ) ) );
                    downLoadEntity.downed = Integer.parseInt( cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_downed ) ) );
                    downLoadEntity.total = Integer.parseInt( cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_total ) ) );
                    downLoadEntity.saveName = cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_saveName ) );
                    downLoadEntity.packageName = cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_packageName ) );
                    downLoadEntity.type = cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_type ) );
                    downLoadEntity.internet = cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_internet ) );
                    downLoadEntity.uninstall = cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_uninstall ) );
                }
            }
            mSQLiteDatabase.setTransactionSuccessful();
            if (cursor != null) {

                cursor.close();
            }
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "queryDownLoadFile" + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }

        closeSQLiteDatabase();
        return downLoadEntity;
    }

    /**
     * 通过sendId获取下载文件信息
     *
     * @param sendId
     * @return
     */
    public synchronized DownLoadEntity queryDownLoadFileBySendId(String sendId) {
        DownLoadEntity downLoadEntity = null;
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();

            Cursor cursor = mSQLiteDatabase.rawQuery( downfile_query_sql_by_sendId, new String[]{sendId} );
            if (cursor != null && cursor.getCount() > 0) {
                if (cursor.moveToNext()) {
                    downLoadEntity = new DownLoadEntity();
                    downLoadEntity.app_id = cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_id ) );
                    downLoadEntity.sendId = cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_sendId ) );
                    downLoadEntity.code = cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_code ) );
                    downLoadEntity.start = Integer.parseInt( cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_start ) ) );
                    downLoadEntity.downed = Integer.parseInt( cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_downed ) ) );
                    downLoadEntity.total = Integer.parseInt( cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_total ) ) );
                    downLoadEntity.saveName = cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_saveName ) );
                    downLoadEntity.packageName = cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_packageName ) );
                    downLoadEntity.type = cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_type ) );
                    downLoadEntity.internet = cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_internet ) );
                    downLoadEntity.uninstall = cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_uninstall ) );
                }
            }
            mSQLiteDatabase.setTransactionSuccessful();
            if (cursor != null) {

                cursor.close();
            }
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "queryDownLoadFile" + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }

        closeSQLiteDatabase();
        return downLoadEntity;
    }

    /**
     * 通过packageName获取下载文件信息
     *
     * @param packageName
     * @return
     */
    public synchronized DownLoadEntity queryDownLoadFileByPackageName(String packageName) {
        DownLoadEntity downLoadEntity = null;
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();

            Cursor cursor = mSQLiteDatabase.rawQuery( downfile_query_sql_by_packagename, new String[]{packageName} );
            if (cursor != null && cursor.getCount() > 0) {
                if (cursor.moveToNext()) {
                    downLoadEntity = new DownLoadEntity();
                    downLoadEntity.app_id = cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_id ) );
                    downLoadEntity.sendId = cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_sendId ) );
                    downLoadEntity.code = cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_code ) );
                    downLoadEntity.start = Integer.parseInt( cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_start ) ) );
                    downLoadEntity.downed = Integer.parseInt( cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_downed ) ) );
                    downLoadEntity.total = Integer.parseInt( cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_total ) ) );
                    downLoadEntity.saveName = cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_saveName ) );
                    downLoadEntity.packageName = cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_packageName ) );
                    downLoadEntity.type = cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_type ) );
                    downLoadEntity.internet = cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_internet ) );
                    downLoadEntity.uninstall = cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_uninstall ) );
                }
            }
            mSQLiteDatabase.setTransactionSuccessful();
            if (cursor != null) {

                cursor.close();
            }
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "queryDownLoadFileByPackageName" + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }

        closeSQLiteDatabase();
        return downLoadEntity;
    }

    /**
     * 读取所有未下载完成的文件信息
     *
     * @return
     */
    public synchronized List<DownLoadEntity> queryAllDownLoadFile() {
        List<DownLoadEntity> downLoadAllList = new ArrayList<>();
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();

            Cursor cursor = mSQLiteDatabase.rawQuery( all_downfile_query_sql, null );
            if (cursor != null && cursor.getCount() > 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    DownLoadEntity downLoadEntity = new DownLoadEntity();
                    downLoadEntity.app_id = cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_id ) );
                    downLoadEntity.sendId = cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_sendId ) );
                    downLoadEntity.code = cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_code ) );
                    downLoadEntity.start = Integer.parseInt( cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_start ) ) );
                    downLoadEntity.downed = Integer.parseInt( cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_downed ) ) );
                    downLoadEntity.total = Integer.parseInt( cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_total ) ) );
                    downLoadEntity.saveName = cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_saveName ) );
                    downLoadEntity.packageName = cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_packageName ) );
                    downLoadEntity.type = cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_type ) );
                    downLoadEntity.internet = cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_internet ) );
                    downLoadEntity.uninstall = cursor.getString( cursor.getColumnIndex( DataBaseHelper.downLoadFile_uninstall ) );
                    downLoadAllList.add( downLoadEntity );
                }
            }
            mSQLiteDatabase.setTransactionSuccessful();
            if (cursor != null) {

                cursor.close();
            }
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "queryAllDownLoadFile" + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        closeSQLiteDatabase();
        return downLoadAllList;
    }

    /**
     * 更新下载文件信息
     *
     * @param downLoadEntity
     */
    public synchronized void updateDownLoadFile(DownLoadEntity downLoadEntity) {
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            mSQLiteDatabase.execSQL( update_downfile_sql, new String[]{String.valueOf( downLoadEntity.downed ), downLoadEntity.saveName, String.valueOf( downLoadEntity.total ), downLoadEntity.app_id} );
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "updateDownLoadFile" + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        closeSQLiteDatabase();
    }

    /**
     * 删除文件下载记录
     *
     * @param downLoadEntity
     */
    public synchronized void deleteDownLoadFile(DownLoadEntity downLoadEntity) {
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            if (queryDownLoadFile( downLoadEntity.app_id ) != null) {
                mSQLiteDatabase.execSQL( delete_downfile_sql, new String[]{String.valueOf( downLoadEntity.app_id )} );
            }
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "deleteDownLoadFile" + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        closeSQLiteDatabase();
    }

    /********************************文件操作*******************************/
    /**
     * 下载的文件添加到相关存储
     *
     * @param downLoadEntity
     */
    public synchronized void insertFile(DownLoadEntity downLoadEntity) {
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            if (queryFile( downLoadEntity ) == null) {
                mSQLiteDatabase.execSQL( add_file_sql, new String[]{downLoadEntity.app_id, downLoadEntity.saveName} );
            }
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "insert file" + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        closeSQLiteDatabase();
    }

    /**
     * 文件查询
     *
     * @param downLoadEntity
     */
    public synchronized String queryFile(DownLoadEntity downLoadEntity) {
        Cursor cursor = null;
        String name = null;
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            cursor = mSQLiteDatabase.rawQuery( query_file_sql, new String[]{downLoadEntity.app_id} );
            if (cursor != null && cursor.getCount() > 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    name = cursor.getString( cursor.getColumnIndex( DataBaseHelper.file_name ) );
                }
            }
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "query file" + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        if (cursor != null) {

            cursor.close();
        }
        closeSQLiteDatabase();
        return name;
    }

    /**
     * 文件删除
     *
     * @param downLoadEntity
     */
    public synchronized void deleteFile(DownLoadEntity downLoadEntity) {
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            mSQLiteDatabase.execSQL( delete_file_sql, new String[]{downLoadEntity.app_id} );
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "delete file" + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        closeSQLiteDatabase();
    }

    /************************通知相关操作*****************************/

    /**
     * 添加通知
     *
     * @param messageInfo
     */
    public synchronized void addMessageInfo(MessageInfo messageInfo) {
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();

            mSQLiteDatabase.execSQL( add_message_sql, new String[]{messageInfo.getMessage_icon(), messageInfo.getMessage_id(),
                    messageInfo.getMessage_from(), messageInfo.getMessage_time(), messageInfo.getMessage_about()} );

            //当消息数量大于200时，删除最先存入的消息
            List<MessageInfo> lists = queryAllMessageInfo();
            if (lists != null && lists.size() > 100) {
                deleteMessageInfo( lists.get( 0 ).getMessage_time() );
                LogUtil.writeToFile( TAG, "delete message " + lists.get( 0 ).getMessage_time() );
            }
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "add message " + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        closeSQLiteDatabase();
    }

    /**
     * 读取所有通知
     *
     * @return
     */
    public synchronized List<MessageInfo> queryAllMessageInfo() {
        Cursor cursor = null;
        List<MessageInfo> messageInfoList = new ArrayList<>();
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            cursor = mSQLiteDatabase.rawQuery( query_message_sql, null );
            if (cursor != null && cursor.getCount() > 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    MessageInfo info = new MessageInfo();
                    info.setMessage_icon( cursor.getString( cursor.getColumnIndex( DataBaseHelper.message_icon ) ) );
                    info.setMessage_id( cursor.getString( cursor.getColumnIndex( DataBaseHelper.message_id ) ) );
                    info.setMessage_from( cursor.getString( cursor.getColumnIndex( DataBaseHelper.message_from ) ) );
                    info.setMessage_time( cursor.getString( cursor.getColumnIndex( DataBaseHelper.message_time ) ) );
                    info.setMessage_about( cursor.getString( cursor.getColumnIndex( DataBaseHelper.message_about ) ) );
                    messageInfoList.add( info );
                }
            }
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "query message " + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        if (cursor != null) {

            cursor.close();
        }
        closeSQLiteDatabase();
        return messageInfoList;
    }

    /**
     * 删除通知
     */
    public synchronized void deleteMessageInfo(String messageTime) {
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            mSQLiteDatabase.execSQL( delete_message_sql, new String[]{messageTime} );
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "delete message " + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        closeSQLiteDatabase();
    }

    /**
     * 更新通知状态
     *
     * @param message_icon
     * @param message_time
     */
    public synchronized void updateMessageInfo(String message_icon, String message_time) {
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            mSQLiteDatabase.execSQL( update_message_sql, new String[]{message_icon, message_time} );
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "update message " + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        closeSQLiteDatabase();
    }

    /************************策略操作*****************************/
    /**
     * 添加策略
     *
     * @param strategeInfo
     */
    public synchronized void addStrategeInfo(StrategeInfo strategeInfo) {
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();

            if (strategeInfo != null) {
                if (queryStrageInfo( strategeInfo.strategeId )) {
                    if (strategeInfo.strategeId.equals(String.valueOf(OrderConfig.SEND_SENSITIVE_WORD_POLICY))) {
                        if (queryStrageInfoByName(strategeInfo.strategeName)) {
//                            updateStrategeInfo(strategeInfo);
                        } else {
                            mSQLiteDatabase.execSQL(add_stratege_sql, new String[]{strategeInfo.strategeId, strategeInfo.strategeName, strategeInfo.strategeTime});
                        }
                    } else {
                        updateStrategeInfo(strategeInfo);
                    }
                } else {

                    switch (Integer.valueOf( strategeInfo.strategeId )) {
                        case OrderConfig.send_geographical_Fence:
                        case OrderConfig.send_time_Frence:
                            deleteSimpleStrategeInfo( OrderConfig.send_geographical_Fence + "" );
                            deleteSimpleStrategeInfo( OrderConfig.send_time_Frence + "" );
                            break;
                    }

                    mSQLiteDatabase.execSQL( add_stratege_sql, new String[]{strategeInfo.strategeId, strategeInfo.strategeName, strategeInfo.strategeTime} );
                }
            }
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "add stratege " + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        closeSQLiteDatabase();
    }

    /**
     * 查询该strategy是否存在
     *
     * @param strategeId
     * @return
     */
    public synchronized boolean queryStrageInfo(String strategeId) {
        openSQLiteDataBase();
        Cursor cursor = null;
        try {
            mSQLiteDatabase.beginTransaction();
            if (strategeId != null) {
                cursor = mSQLiteDatabase.rawQuery( query_stratege_sql, new String[]{strategeId} );
            }

            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "query strategy " + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }

        int num = cursor.getCount();
        if (cursor != null) {

            cursor.close();
        }
        closeSQLiteDatabase();

        if (num == 0) {
            return false;
        }

        return true;
    }

    /**
     * 查询策略名对应的strategy是否存在
     *for sensitive word
     *
     * @param strategyName
     * @return
     */
    public synchronized boolean queryStrageInfoByName(String strategyName) {
        openSQLiteDataBase();
        Cursor cursor = null;
        try {
            mSQLiteDatabase.beginTransaction();
            if (strategyName != null) {
                cursor = mSQLiteDatabase.rawQuery( query_strategy_sql_by_name, new String[]{strategyName} );
            }

            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "query strategy by name " + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }

        int num = cursor.getCount();
        if (cursor != null) {

            cursor.close();
        }
        closeSQLiteDatabase();

        if (num == 0) {
            return false;
        }

        return true;
    }

    /**
     * 读取所有策略
     *
     * @return
     */
    public synchronized List<StrategeInfo> queryAllStrategeInfo() {
        Cursor cursor = null;
        List<StrategeInfo> strategeInfoList = new ArrayList<>();
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            cursor = mSQLiteDatabase.rawQuery( query_all_stratege_sql, null );
            if (cursor != null && cursor.getCount() > 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    StrategeInfo info = new StrategeInfo();
                    info.strategeId = cursor.getString( cursor.getColumnIndex( DataBaseHelper.STRATEGY_CODE) );
                    info.strategeName = cursor.getString( cursor.getColumnIndex( DataBaseHelper.stratege_name ) );
                    info.strategeTime = cursor.getString( cursor.getColumnIndex( DataBaseHelper.stratege_time ) );
                    strategeInfoList.add( info );
                }
            }
            mSQLiteDatabase.setTransactionSuccessful();

        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "query all stratege " + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        if (cursor != null) {

            cursor.close();
        }
        closeSQLiteDatabase();
        return strategeInfoList;
    }

    public synchronized void updateStrategeInfo(StrategeInfo strategeInfo) {
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            if (strategeInfo != null) {
                mSQLiteDatabase.execSQL(update_stratege_sql, new String[]{strategeInfo.strategeName, strategeInfo.strategeTime, strategeInfo.strategeId});
            }
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "update strategy " + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        closeSQLiteDatabase();
    }

    /**
     * 删除策略
     */
    public synchronized void deleteStrategeInfo() {
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            mSQLiteDatabase.execSQL( delete_stratege_sql );
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "delete strategr " + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        closeSQLiteDatabase();
    }

    /**
     * 删除一个策略
     *
     * @param strategeId
     */
    public synchronized void deleteSimpleStrategeInfo(String strategeId) {
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            mSQLiteDatabase.execSQL( delete_simple_stratege_sql, new String[]{strategeId} );
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "delete simple strategr " + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        closeSQLiteDatabase();
    }

    /**
     * 通过ordercode和name删除一个策略
     * for sensitive word
     * @param strategyId
     * @param strategyName
     */
    public synchronized void deleteSimpleStrategeInfoByName(String strategyId, String strategyName) {
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            mSQLiteDatabase.execSQL(delete_simple_strategy_sql_by_name, new String[]{strategyId, strategyName} );
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile(TAG, "delete simple strategy by name " + e.toString());
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        closeSQLiteDatabase();
    }

    /************************应用名单操作*****************************/
    /**
     * 添加应用名单
     *
     * @param whiteList
     */
    public synchronized void addAppWhiteList(List<String> whiteList) {
        if (whiteList == null) {
            return;
        }

        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();

            for (String app : whiteList) {
                mSQLiteDatabase.execSQL( add_app_compliance_sql, new String[]{app} );
            }
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "add app " + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        closeSQLiteDatabase();
    }

    /**
     * 读取所有应用名单
     *
     * @return
     */
    public synchronized List<String> queryAllApp() {
        Cursor cursor = null;
        List<String> appList = new ArrayList<>();
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            cursor = mSQLiteDatabase.rawQuery( query_all_app_compliance_sql, null );
            if (cursor != null && cursor.getCount() > 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    String app = cursor.getString( cursor.getColumnIndex( DataBaseHelper.app_compliance_name ) );
                    appList.add( app );
                }
            }
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "query all app " + LogUtil.getExceptionInfo( e ) );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        if (cursor != null) {

            cursor.close();
        }
        closeSQLiteDatabase();
        return appList;
    }

    /**
     * 删除所有应用名单
     */
    public synchronized void deleteAllApp() {
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            mSQLiteDatabase.execSQL( delete_all_app_compliance_sql );
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "delete all app " + LogUtil.getExceptionInfo( e ) );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        closeSQLiteDatabase();
    }

    /**
     * 删除应用名单
     */
    public synchronized void deleteSimpleApp(String packageName) {
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            mSQLiteDatabase.execSQL( delete_app_compliance_sql, new String[]{packageName} );
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "delete simple app " + LogUtil.getExceptionInfo( e ) );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        closeSQLiteDatabase();
    }

    /************************违规应用名单操作*****************************/
    /**
     * 添加违规应用名单
     *
     * @param whiteList
     */
    public synchronized void addAppDenyList(Map<String, String> whiteList) {
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            Iterator<Map.Entry<String, String>> iterator = whiteList.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                if (queryDenyAppByName( entry.getValue() ).size() > 0) {
                    updateDenyApp( entry.getValue(), "0" );
                } else {
                    mSQLiteDatabase.execSQL( add_app_deny_sql, new String[]{entry.getKey(), "0", entry.getValue()} );
                }
            }
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "add deny app " + LogUtil.getExceptionInfo( e ) );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        closeSQLiteDatabase();
    }

    /**
     * 读取所有违规应用名单
     *
     * @return
     */
    public synchronized List<String> queryAllDenyApp() {
        Cursor cursor = null;
        List<String> appList = new ArrayList<>();
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            cursor = mSQLiteDatabase.rawQuery( query_all_app_deny_sql, null );
            if (cursor != null && cursor.getCount() > 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    String app = cursor.getString( cursor.getColumnIndex( DataBaseHelper.app_deny_name ) );
                    appList.add( app );
                }
            }
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "query all deny app " + LogUtil.getExceptionInfo( e ) );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        if (cursor != null) {

            cursor.close();
        }
        closeSQLiteDatabase();
        return appList;
    }

    /**
     * 更新状态
     *
     * @param name
     */
    public synchronized void updateDenyApp(String name, String type) {
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            mSQLiteDatabase.execSQL( update_app_deny, new String[]{type, name} );
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "delete all deny app " + LogUtil.getExceptionInfo( e ) );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        closeSQLiteDatabase();
    }

    /**
     * 删除所有违规应用
     */
    public synchronized void deleteAllDenyApp() {
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            mSQLiteDatabase.execSQL( delete_all_app_deny_sql );
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "delete all deny app " + LogUtil.getExceptionInfo( e ) );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        closeSQLiteDatabase();
    }


    /**
     * 通过type读取违规应用
     */
    public synchronized List<String> queryDenyAppByType(String type) {
        Cursor cursor = null;
        List<String> appList = new ArrayList<>();

        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            cursor = mSQLiteDatabase.rawQuery( query_app_deny_from_type, new String[]{type} );
            if (cursor != null && cursor.getCount() > 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    String app = cursor.getString( cursor.getColumnIndex( DataBaseHelper.app_deny_name ) );
                    appList.add( app );
                }
            }
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "query deny app by type " + LogUtil.getExceptionInfo( e ) );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        if (cursor != null) {

            cursor.close();
        }
        closeSQLiteDatabase();

        return appList;
    }

    /**
     * 通过name读取违规应用
     */
    public synchronized List<String> queryDenyAppByName(String name) {

        Cursor cursor = null;
        List<String> appList = new ArrayList<>();
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            cursor = mSQLiteDatabase.rawQuery( query_app_deny_from_name, new String[]{name} );
            if (cursor != null && cursor.getCount() > 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    String app = cursor.getString( cursor.getColumnIndex( DataBaseHelper.app_deny_name ) );
                    appList.add( app );
                }
            }
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "query deny app by name " + LogUtil.getExceptionInfo( e ) );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        if (cursor != null) {

            cursor.close();
        }
        closeSQLiteDatabase();

        return appList;
    }
    /****************************存储上传失败的数据**************************************/
    /**
     * 存储上传失败的数据
     *
     * @param type 对应的类型
     * @param data
     */
    public synchronized void add_backResult_sql(String type, String data) {
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();

            //boolean hadAdd = false;

            //List<MessageResendData> mList = queryAll_backResult_sql();

            //if (mList != null && mList.size() > 0) {
                //for (MessageResendData messageResendData : mList) {
                    /*if (messageResendData.resend_type.equals(type)) {
                        //hadAdd = true;
                        List<String> ids = new ArrayList<>();
                        ids.add(messageResendData.resend_id);
                        delete_id_backResult_sql(ids);
                        //mSQLiteDatabase.execSQL( add_backResult_sql, new String[]{type, data} );
                    }*/
                    mSQLiteDatabase.execSQL( add_backResult_sql, new String[]{type, data} );
                //}

                //if (!hadAdd) {
                    //mSQLiteDatabase.execSQL( add_backResult_sql, new String[]{type, data} );
                //}
            //} /*else {
                //mSQLiteDatabase.execSQL( add_backResult_sql, new String[]{type, data} );
            //}

            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "add_backResult_sqlr " + LogUtil.getExceptionInfo( e ) );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        closeSQLiteDatabase();
    }

    /**
     * 获取所有的存储返回失败的数据
     *
     * @return
     */
    public synchronized List<MessageSendData> queryAll_backResult_sql() {
        Cursor cursor = null;
        List<MessageSendData> lists = new ArrayList<>();
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            cursor = mSQLiteDatabase.rawQuery( queryAll_backResult_sql, null );
            if (cursor != null && cursor.getCount() > 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

                    String resendCode = cursor.getString( cursor.getColumnIndex( DataBaseHelper.backResult_type ) );
                    String resendId = cursor.getString( cursor.getColumnIndex( "_id" ) );
                    String resendContent = cursor.getString( cursor.getColumnIndex( DataBaseHelper.backResult_data ) );

                    MessageSendData data = new MessageSendData(Integer.parseInt(resendCode), resendContent, true);
                    data.setId(resendId);
                    lists.add( data );
                }
            }
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "query_backResult_sql " + LogUtil.getExceptionInfo( e ) );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        closeSQLiteDatabase();
        return lists;
    }

    /**
     * 删除对应类型的数据
     *
     * @param ids
     */
    public synchronized void delete_id_backResult_sql(List<String> ids) {
        if (ids == null || ids.size() == 0) {
            return;
        }

        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            for (String id : ids) {
                mSQLiteDatabase.execSQL( delete_id_backResult_sql, new String[]{id} );
            }
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "delete_backResult_sql" + LogUtil.getExceptionInfo( e ) );
            Log.w( TAG, "delete_backResult_sql" + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        closeSQLiteDatabase();
    }

    /**
     * 添加命令完成结果
     * @param type
     * @param result
     * @param time
     */
    public synchronized void addCompleteResult(String type, String result , String time, String id) {
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            CompleteMessageData mCompleteMessageData = queryCompleteResultSql(type, id);

            if (mCompleteMessageData.type != null && id.equals( mCompleteMessageData.id )) {
                updateCompleteResult( type, result, id  );
            } else {
                mSQLiteDatabase.execSQL( add_completeResult_sql, new String[]{type, result, time,id} );
            }
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "add_completeResult_sql " + LogUtil.getExceptionInfo( e ) );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        closeSQLiteDatabase();
    }

    /**
     * 读取所有命令完成消息
     * @return
     */
    public synchronized List<CompleteMessageData> queryAllCompleteResultSql() {
        Cursor cursor = null;
        List<CompleteMessageData> lists = new ArrayList<>();
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            cursor = mSQLiteDatabase.rawQuery( queryAll_completeResult_sql, null );
            if (cursor != null && cursor.getCount() > 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    CompleteMessageData mCompleteMessageData = new CompleteMessageData();

                    mCompleteMessageData.type = cursor.getString( cursor.getColumnIndex( DataBaseHelper.complete_type ) );
                    mCompleteMessageData.result = cursor.getString( cursor.getColumnIndex( DataBaseHelper.complete_result ) );
                    mCompleteMessageData.time = cursor.getString( cursor.getColumnIndex( DataBaseHelper.complete_time ) );
                    mCompleteMessageData.id = cursor.getString( cursor.getColumnIndex( DataBaseHelper.complete_id ) );
                    lists.add( mCompleteMessageData );
                }
            }
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "queryAll_completeResult_sql " + LogUtil.getExceptionInfo( e ) );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        closeSQLiteDatabase();
        return lists;
    }

    /**
     * 根据type查询
     * @param type
     * @return
     */
    public synchronized List<CompleteMessageData> queryCompleteResultTypeSql(String type) {

        Cursor cursor = null;
        List<CompleteMessageData> mCompleteMessageDatas = new ArrayList<>();
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            cursor = mSQLiteDatabase.rawQuery( query_completeResultType_sql, new String[]{type} );
            if (cursor != null && cursor.getCount() > 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    CompleteMessageData mCompleteMessageData = new CompleteMessageData();
                    mCompleteMessageData.type = cursor.getString( cursor.getColumnIndex( DataBaseHelper.complete_type ) );
                    mCompleteMessageData.result = cursor.getString( cursor.getColumnIndex( DataBaseHelper.complete_result ) );
                    mCompleteMessageData.time = cursor.getString( cursor.getColumnIndex( DataBaseHelper.complete_time ) );
                    mCompleteMessageData.id = cursor.getString( cursor.getColumnIndex( DataBaseHelper.complete_id ) );
                    mCompleteMessageDatas.add( mCompleteMessageData );
                }
            }
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "query complete result by type " + LogUtil.getExceptionInfo( e ) );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        if (cursor != null) {

            cursor.close();
        }
        closeSQLiteDatabase();

        return mCompleteMessageDatas;
    }

    /**
     * 根据type与id查询
     * @param type
     * @return
     */
    public synchronized CompleteMessageData queryCompleteResultSql(String type, String id) {

        Cursor cursor = null;
        CompleteMessageData mCompleteMessageData = new CompleteMessageData();
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            cursor = mSQLiteDatabase.rawQuery( query_completeResult_sql, new String[]{type,id} );
            if (cursor != null && cursor.getCount() > 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    mCompleteMessageData.type = cursor.getString( cursor.getColumnIndex( DataBaseHelper.complete_type ) );
                    mCompleteMessageData.result = cursor.getString( cursor.getColumnIndex( DataBaseHelper.complete_result ) );
                    mCompleteMessageData.time = cursor.getString( cursor.getColumnIndex( DataBaseHelper.complete_time ) );
                    mCompleteMessageData.id = cursor.getString( cursor.getColumnIndex( DataBaseHelper.complete_id ) );
                }
            }
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "query complete result by type " + LogUtil.getExceptionInfo( e ) );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        if (cursor != null) {

            cursor.close();
        }
        closeSQLiteDatabase();

        return mCompleteMessageData;
    }

    /**
     * 更新完成结果
     * @param type
     * @param result
     */
    public synchronized void updateCompleteResult( String type, String result, String id ) {
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            mSQLiteDatabase.execSQL( update_completeResult_sql, new String[]{ result, type, id } );
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "update complete result " + LogUtil.getExceptionInfo( e ) );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        closeSQLiteDatabase();
    }

    /**
     * 更新完成时间
     * @param type
     * @param time
     */
    public synchronized void updateCompleteTime( String type, String time ) {
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            mSQLiteDatabase.execSQL( update_completeTime_sql, new String[]{time, type } );
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "update complete time " + LogUtil.getExceptionInfo( e ) );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        closeSQLiteDatabase();
    }

    /**
     * 更新完成结果与时间
     * @param type
     * @param result
     * @param time
     */
    public synchronized void updateCompleteResultAndTime( String type, String result, String time, String id ) {
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            mSQLiteDatabase.execSQL( update_completeResultAndTime_sql, new String[]{result, time, type, id } );
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "update complete time " + LogUtil.getExceptionInfo( e ) );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        closeSQLiteDatabase();
    }

    /**
     * 根据type删除
     * @param type
     */
    public synchronized void deleteCompleteResultSql(String type, String id ) {

        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();

            mSQLiteDatabase.execSQL( delete_completeResult_sql, new String[]{type, id} );

            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "delete_completeResult_sql" + LogUtil.getExceptionInfo( e ) );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        closeSQLiteDatabase();
    }

    /**
     * insert sensitive_word_strategy into db
     * @param info sensitive word strategy information including id, sensitive words and strategy name
     * @return insert successful or not
     */
    public synchronized boolean insertSensitiveWord(SensitiveStrategyInfo info) {
        boolean insertSuccess = false;
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            mSQLiteDatabase.execSQL(add_sensitiveWord_sql, new String[]{info.getId(), info.getSensiWord(), info.getStrategeName()} );
            Log.d("baii", "insert " + info.getId() + " " + info.getSensiWord());
            mSQLiteDatabase.setTransactionSuccessful();
            insertSuccess = true;
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "add sensitive word " + LogUtil.getExceptionInfo( e ) );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        closeSQLiteDatabase();
        return insertSuccess;
    }

    /**
     * query all the sensitive_word_strategy info and put them into a hashmap
     * @return HashMap<id, sensitiveStrategyInfo>
     */
    public synchronized HashMap<String ,SensitiveStrategyInfo> querySensitiveWordAll() {

        HashMap<String ,SensitiveStrategyInfo> map = new HashMap();
        Cursor cursor = null;
        openSQLiteDataBase();

        mSQLiteDatabase.beginTransaction();
        try {
            cursor = mSQLiteDatabase.rawQuery( queryAll_sensitiveWord_sql, null);
            if (null != cursor && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    SensitiveStrategyInfo info = new SensitiveStrategyInfo();
                    info.setId(cursor.getString(cursor.getColumnIndex(DataBaseHelper.SW_STRATEGY_ID)));
                    info.setStrategeName(cursor.getString(cursor.getColumnIndex(DataBaseHelper.NAME)));
                    info.setSensiWord(cursor.getString(cursor.getColumnIndex(DataBaseHelper.WORDS)));
                    map.put(info.getId(), info);
                }
                mSQLiteDatabase.setTransactionSuccessful();
            }
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "querySensitiveWordAll" + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        if (cursor != null) {
            cursor.close();
        }
        closeSQLiteDatabase();
        return map;
    }

    /**
     * query specific sensitive_word_info and return whether exists
     * @param ssinfo
     * @return
     */
    public synchronized boolean hasSensitiveWord(SensitiveStrategyInfo ssinfo) {

        Cursor cursor = null;
        openSQLiteDataBase();

        mSQLiteDatabase.beginTransaction();
        try {
            cursor = mSQLiteDatabase.rawQuery( query_sensitiveWord_sql, new String[]{ssinfo.getId()});
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "querySensitiveWord" + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        int num = 0;
        if (null != cursor) {
            num = cursor.getCount();
            cursor.close();
        }
        closeSQLiteDatabase();
        if (num > 0 ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * query specific sensitive_word_info, get sensitive word
     * @param strategyName
     * @return
     */
    public synchronized String querySensitiveWord(String strategyName) {

        Cursor cursor = null;
        String sensiWord = null;
        openSQLiteDataBase();

        mSQLiteDatabase.beginTransaction();
        try {
            cursor = mSQLiteDatabase.rawQuery( query_sensitiveWord_sql_by_name, new String[]{strategyName});
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "querySensitiveWord" + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        if (null != cursor && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                sensiWord = cursor.getString(cursor.getColumnIndex(DataBaseHelper.WORDS));
            }
            cursor.close();
        }
        closeSQLiteDatabase();
        return sensiWord;
    }

    /**
     * delete specific sensitive_word_strategy from db
     * @param info sensitive_word_strategy to delete
     */
    public synchronized void deleteSensitiveWord(SensitiveStrategyInfo info) {
        openSQLiteDataBase();
        try {
            mSQLiteDatabase.beginTransaction();
            mSQLiteDatabase.execSQL(delete_sensitiveWord_sql, new String[]{info.getId()} );
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "deleteSensitiveWord" + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        closeSQLiteDatabase();
    }

    /**
     * 更新敏感词策略
     *
     * @param info
     */
    public synchronized void updateSensitiveWord(SensitiveStrategyInfo info) {
        openSQLiteDataBase();

        try {
            mSQLiteDatabase.beginTransaction();
            mSQLiteDatabase.execSQL(update_sensitiveWord_sql, new String[]{info.getSensiWord(), info.getId()} );
            mSQLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, "updateSensitiveWord" + e.toString() );
        } finally {
            mSQLiteDatabase.endTransaction();
        }
        //   }
        closeSQLiteDatabase();
    }

}