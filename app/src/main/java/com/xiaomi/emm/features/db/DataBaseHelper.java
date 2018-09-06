package com.xiaomi.emm.features.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.xiaomi.emm.utils.LogUtil;

/**
 * Created by Administrator on 2017/6/13.
 */

public class DataBaseHelper extends SQLiteOpenHelper {
    private static final String TAG = DataBaseHelper.class.getName();

    //单例
    private volatile static DataBaseHelper mDataBaseHelper;
    private static final String database_name = "MDM.db";

    //电话白名单
    public static final String white_table = "white_list";
    public static final String white_name = "name";
    public static final String white_id = "id";
    public static final String white_phone = "phone";
    public static final String white_address = "address";
    public static final String white_shortPhoneNum = "shortPhoneNum";
    public static final String white_teamName = "teamName";
    public static final String white_loginName = "loginName";

    //应用安装
    public static final String installApp_table = "install_applist";
    public static final String app_id = "app_id";
    public static final String app_name = "app_name";
    public static final String package_name = "package_name";

    //文件下载
    public static final String downLoadFile_table = "download_file";
    public static final String downLoadFile_id = "app_id";
    public static final String downLoadFile_sendId = "sendId";
    public static final String downLoadFile_code = "app_code";
    public static final String downLoadFile_start = "start";
    public static final String downLoadFile_downed = "downed";
    public static final String downLoadFile_total = "total";
    public static final String downLoadFile_saveName = "save_name";
    public static final String downLoadFile_packageName = "package_name";
    public static final String downLoadFile_type = "file_type";
    public static final String downLoadFile_internet = "file_internet";
    public static final String downLoadFile_uninstall = "file_uninstall";

    //通知
    public static final String message_table = "message";
    public static final String message_icon = "message_icon";
    public static final String message_id = "message_id";
    public static final String message_from = "message_from";
    public static final String message_time = "message_time";
    public static final String message_about  = "message_about";

    //文件
    public static final String file_table = "file";
    public static final String file_id = "file_id";
    public static final String file_name = "file_name";

    //策略
    public static final String stratege_table = "stratege";
    public static final String STRATEGY_ID = "strategy_id";
    public static final String STRATEGY_CODE = "stratege_code";
    public static final String stratege_name = "stratege_name";
    public static final String stratege_time = "stratege_time";

    //应用名单
    public static final String app_table = "app_compliance";
    public static final String app_compliance_name = "app_compliance_name";

    //违规应用名单
    public static final String app_deny = "app_deny";
    public static final String app_deny_type = "app_deny_type";
    public static final String app_deny_name = "app_deny_name";
    public static final String app_deny_package = "app_deny_package";

    //敏感词
    protected static final String SENSITIVE_WORD_TABLE = "sensitive_word";
    public static final String SW_STRATEGY_ID = "strategy_id";
    public static final String WORDS = "words";
    public static final String NAME = "strategy_name";

    //返回结果 失败存储表
    public static final String backResult_table = "backResult_table";   //
    public static final String backResult_type= "backResult_type";      // id对应的标志(有返回结果是失败存储 ，有发送其他发送失败的存储)
    public static final String backResult_data = "backResult_data";  //存储的内容(json格式存储)

    //命令执行完成返回
    public static final String complete_table = "complete_table";
    public static final String complete_type = "complete_type";
    public static final String complete_result = "complete_result";
    public static final String complete_time = "complete_time";
    public static final String complete_id = "complete_id";

    private static final int VERSION = 2;
    private DataBaseHelper(Context context) {
        super(context, database_name, null,VERSION);
    }

    //单例
    public static DataBaseHelper getSingleInstance(Context context) {
        if (null == mDataBaseHelper) {
            synchronized (DataBaseHelper.class) {
                if (null == mDataBaseHelper) {
                    mDataBaseHelper = new DataBaseHelper(context);
                }
            }
        }
        return mDataBaseHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Telephony white list
        db.execSQL("CREATE TABLE "+ white_table + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                white_name + " TEXT," + white_id + " TEXT," + white_phone + " TEXT," + white_address + " TEXT," + white_shortPhoneNum + " TEXT," + white_loginName + " TEXT," + white_teamName + " TEXT);");

        // Install app
        db.execSQL("CREATE TABLE " + installApp_table + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                app_id + " TEXT,"+ app_name + " TEXT," + package_name + " TEXT);");
        //Download file
        db.execSQL("CREATE TABLE " + downLoadFile_table + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                downLoadFile_id + " TEXT," + downLoadFile_sendId + " TEXT," + downLoadFile_code + " TEXT," + downLoadFile_start + " TEXT," + downLoadFile_downed +
                " TEXT," + downLoadFile_total + " TEXT," + downLoadFile_saveName + " TEXT," + downLoadFile_packageName + " TEXT,"
                + downLoadFile_internet + " TEXT,"  + downLoadFile_uninstall + " TEXT," + downLoadFile_type +" TEXT);");
        //Message
        db.execSQL("CREATE TABLE " + message_table + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                message_icon + " TEXT," + message_id + " TEXT," + message_from + " TEXT," + message_time +
                " TEXT," + message_about + " TEXT);");
        //File
        db.execSQL("CREATE TABLE " + file_table + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                file_id + " TEXT,"+ file_name + " TEXT);");

        //lost compliance
        db.execSQL("CREATE TABLE " + stratege_table + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                STRATEGY_CODE + " TEXT," + STRATEGY_ID + " TEXT," + stratege_name + " TEXT," + stratege_time + " TEXT);");

        //app compliance
        db.execSQL("CREATE TABLE " + app_table + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                app_compliance_name + " TEXT);");

        //app compliance deny
        db.execSQL("CREATE TABLE " + app_deny + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                app_deny_name + " TEXT," + app_deny_type +  " TEXT," + app_deny_package + " TEXT);");

        //创建一张返回结果code临时存储表
        db.execSQL("CREATE TABLE " + backResult_table + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                backResult_type + " TEXT," + backResult_data + " TEXT);");

        //执行命令完成返回
        db.execSQL("CREATE TABLE " + complete_table + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                complete_type + " TEXT," + complete_result + " TEXT," + complete_time + " TEXT," + complete_id +" TEXT);");

        //sensitive word strategy
        db.execSQL("CREATE TABLE IF NOT EXISTS " + SENSITIVE_WORD_TABLE + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                SW_STRATEGY_ID + " TEXT," + NAME + " TEXT," + WORDS + " TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LogUtil.writeToFile(TAG, "db update from " + oldVersion + " to " + newVersion);
    }
}