package com.xiaomi.emm.utils;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Administrator on 2017/7/17.
 */

public class PreferencesManager {
    public final static String TAG = "PreferencesManager";
    public final static String MDM_NAME = "MDM";
    public final static String COMPALIANCE_NAME = "Compliance";
    public final static String POLICY_NAME = "Policy";
    public final static String FENCE_NAME = "Fence";
    public static final String NUMBERLOCK_NAME = "numberlock";
    public static final String Traffic_NAME = "traffictotal";

    public static final String TIMEFENCE_NAME = "TimeFence";
    public static final String SAFEDESKTOP_NAME = "safedesktop";
    public static final String Configuration_NAME = "Configuration_name";//ConfigurationPolicy

    public static final String SWITCH_LOG = "switchLog";
    public static final String IncomingNumber_Log = "IncomingNumberLog";

    public static final String SETTING = "Setting";

    public static final String TRAJECTORY_NAME = "trajectory";

    public static final String SECURITY = "security";

    //public static final String Machine = "machine";

    public static final String APPFence = "appfence";

    public static final String Other = "other";

    private SharedPreferences sharedPreferences;

    private SharedPreferences compliancePreferences;
    private SharedPreferences policyPreferences;
    private SharedPreferences fencePreferences;
    private SharedPreferences numberlockPreferences;
    private SharedPreferences traffictotalPreferences;
    private SharedPreferences timeFencePreferences;
    private SharedPreferences safeDesktopPreferences;
    private SharedPreferences ConfigurationPreferences;

    private SharedPreferences LogPreferences;
    private SharedPreferences IncomingNumberLogPreferences;

    private SharedPreferences SettingPreferences;
    private SharedPreferences securityPreferences;

    //private SharedPreferences machinePreferences;

    private SharedPreferences appFencePreferences;
    private SharedPreferences trajectoryPreferences;
    private SharedPreferences otherPreferences;
    private Context mContext;

    private static volatile PreferencesManager preferencesManager = null;

    public void init(Context context) {

        sharedPreferences = context.getSharedPreferences(MDM_NAME, MODE_PRIVATE);

        compliancePreferences = context.getSharedPreferences(COMPALIANCE_NAME, MODE_PRIVATE);
        policyPreferences = context.getSharedPreferences(POLICY_NAME, MODE_PRIVATE);
        fencePreferences = context.getSharedPreferences(FENCE_NAME, MODE_PRIVATE);
       // fencePreferences = context.getSharedPreferences(FENCE_NAME, MODE_PRIVATE);
        numberlockPreferences = context.getSharedPreferences(NUMBERLOCK_NAME, MODE_PRIVATE);
        traffictotalPreferences = context.getSharedPreferences(Traffic_NAME, MODE_PRIVATE);
        timeFencePreferences = context.getSharedPreferences(TIMEFENCE_NAME, MODE_PRIVATE);
        safeDesktopPreferences = context.getSharedPreferences(SAFEDESKTOP_NAME, MODE_PRIVATE);
        ConfigurationPreferences = context.getSharedPreferences(Configuration_NAME, MODE_PRIVATE);

        LogPreferences = context.getSharedPreferences(SWITCH_LOG, MODE_PRIVATE);
        IncomingNumberLogPreferences = context.getSharedPreferences(IncomingNumber_Log, MODE_PRIVATE);

        SettingPreferences = context.getSharedPreferences( SETTING, MODE_PRIVATE );

        securityPreferences = context.getSharedPreferences( SECURITY, MODE_PRIVATE );

        //machinePreferences = context.getSharedPreferences( Machine, MODE_PRIVATE );
        trajectoryPreferences = context.getSharedPreferences( TRAJECTORY_NAME, MODE_PRIVATE );
        appFencePreferences = context.getSharedPreferences( APPFence, MODE_PRIVATE );

        otherPreferences = context.getSharedPreferences( Other, MODE_PRIVATE );

        mContext = context;
        LogUtil.writeToFile(TAG, "PreferencesManager init!");
    }

    private PreferencesManager() {

    }

    public synchronized static PreferencesManager getSingleInstance() {
        try {
            if (null == preferencesManager) {
                //synchronized (PreferencesManager.class) {
                  //  if (null == preferencesManager) {
                        preferencesManager = new PreferencesManager();
                   // }
                //}
            }
        } catch (Exception e) {
            LogUtil.writeToFile( TAG, e.toString());
        }
        return preferencesManager;
    }

    //mdm
    public void setData(String key, String value) {
        //存入数据
        setBaseData( key, value, sharedPreferences );
    }

    public String getData(String key) {
        return getBaseData( key, sharedPreferences );
    }

    public void removeData(String key) {
        removeBaseData( key, sharedPreferences );
    }

    //policy
    public void setPolicyData(String key, String value) {
        //存入数据
        setBaseData( key, value, policyPreferences );
    }

    public String getPolicyData(String key) {
        return getBaseData( key, policyPreferences );
    }

    public void removePolicyData(String key) {
        removeBaseData( key, policyPreferences );
    }

    //compliance
    public void setComplianceData(String key, String value) {
        //存入数据
        setBaseData( key, value, compliancePreferences );
    }

    public String getComplianceData(String key) {
        return getBaseData( key, compliancePreferences );
    }

    public void removeComplianceData(String key) {
        removeBaseData( key, compliancePreferences );
    }

    //fence
    public void setFenceData(String key, String value) {
        //存入数据
        setBaseData( key, value, fencePreferences );
    }

    public String getFenceData(String key) {
        return getBaseData( key, fencePreferences );
    }

    public void removeFenceData(String key) {
        removeBaseData( key, fencePreferences );
    }

    //清除围栏数据
    public void clearFenceData() {
        clearBaseData(fencePreferences);
    }

    //
    public void setLockPassword(String key, String value) {
        setBaseData( key, value, numberlockPreferences );
    }

    /**
     * 屏保密码
     * @param key
     */
    public void removePassword(String key) {
        removeBaseData( key, numberlockPreferences );
    }
    public String getLockPassword(String key){
        return getBaseData( key, numberlockPreferences );
    }

    public void removeLockPassword(String key){
        removeBaseData( key, numberlockPreferences );
    }

    public void  setLockFlag(String key,boolean value){

        SharedPreferences.Editor editor = numberlockPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();

    }
    public void  removeLockFlag(String key){

        SharedPreferences.Editor editor = numberlockPreferences.edit();
        editor.remove(key);
        editor.commit();

    }

    public boolean getLockFlag(String key){
        return numberlockPreferences.getBoolean(key,false);
    }

    public void setTraffictotal(String key, long value) {
        //存入数据
        SharedPreferences.Editor editor = traffictotalPreferences.edit();
        editor.putLong(key, value);
        editor.commit();

    }

    public long getTraffictotal(String key){
        return traffictotalPreferences.getLong(key, 0);
    }
    public void  removeTraffictotal(String key) {
        traffictotalPreferences.edit().remove(key);
    }


    //timeFence
    public void setTimefenceData(String key,String value){
        //存入数据
        setBaseData( key, value, timeFencePreferences );
    }
    //删除某个
    public void removeTimefenceData(String key){
        //存入数据
        removeBaseData( key, timeFencePreferences );
    }

    public String getTimefenceData(String key){
        return getBaseData( key, timeFencePreferences );
    }

    public void clearTimefenceData(){
        //清除数据
        clearBaseData(timeFencePreferences);
    }

    /**
     * 存安全桌面策略
     * @param key
     * @param value
     */
    public void setSafedesktopData(String key,String value){
        setBaseData( key, value, safeDesktopPreferences );
    }

    /**
     * 获取安全桌面策略数据
     * @param key
     * @return
     */
    public  String getSafedesktopData(String key){
        return getBaseData( key, safeDesktopPreferences );
    }

    public  void clearSafedesktopData(){
        clearBaseData(safeDesktopPreferences);
    }


    public void setConfiguration(String key,String value){
        setBaseData( key, value, ConfigurationPreferences );
    }

    public  String getConfiguration(String key){

        return getBaseData( key, ConfigurationPreferences );
    }

    public  void removeConfiguration(String key){
        removeBaseData( key, ConfigurationPreferences );
    }


    public  void clearConfiguration(){
        clearBaseData(ConfigurationPreferences);
    }

    public void setLogData(String key, String value ) {
        setBaseData( key, value, LogPreferences );
    }




    public  String getLogData(String key){
        return getBaseData( key, LogPreferences );
    }


    public  void removeLogData(String key){
        removeBaseData( key, LogPreferences );
    }

    //来电日志记录  IncomingNumberLogPreferences
    public void setComingNumberLog(String key, String value ) {
        setBaseData( key, value, IncomingNumberLogPreferences );
    }

    public  String getComingNumberLog(String key){
        return getBaseData( key, IncomingNumberLogPreferences );
    }


    public  void clearComingNumberLog(){
        clearBaseData(IncomingNumberLogPreferences);
    }

    //设置：帮助、支持、许可协议
    public void setSettingData(String key, String value) {
        setBaseData( key, value, SettingPreferences );
    }

    public  String getSettingData(String key){
        return getBaseData( key, SettingPreferences );
    }

    //安全配置策略
    public void setSecurityData(String key, String value) {
        setBaseData( key, value, securityPreferences );
    }

    public String getSecurityData( String key ) {
        return getBaseData( key, securityPreferences );
    }

    public void removeSecurityData( String key ) {
        removeBaseData( key, securityPreferences );
    }

    //机卡绑定
    /*public void setMachineData(String key, String value) {
        setBaseData( key, value, machinePreferences );
    }

    public void removeMachineData(String key) {
        removeBaseData( key, machinePreferences );
    }

    public String getMachineData( String key ) {
        return getBaseData( key, machinePreferences );
    }*/

    //应用限制策略
    public void setAppFenceData(String key, String value) {
        setBaseData( key, value, appFencePreferences );
    }

    public String getAppFenceData( String key ) {
        return getBaseData( key, appFencePreferences );
    }

    public void removeAppFenceData( String key ) {
        removeBaseData( key, appFencePreferences );
    }

    public void clearAppFenceData( ) {
        appFencePreferences.edit().clear().commit();
    }

    //其他数据存储
    public void setOtherData(String key, String value) {
        setBaseData( key, value, otherPreferences );
    }

    public String getOtherData( String key ) {
        return getBaseData( key, otherPreferences );
    }

    public void removeOtherData( String key) {
        removeBaseData(key, otherPreferences);
    }
    /**
     * 基本操作 添加
     * @param key
     * @param value
     * @param sharedPreferences
     */
    public void setBaseData(String key, String value, SharedPreferences sharedPreferences) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key,value);
        editor.commit();
    }

    /**
     * 基本操作 获取
     * @param key
     * @param sharedPreferences
     * @return
     */
    public String getBaseData(String key, SharedPreferences sharedPreferences) {
        return sharedPreferences.getString(key,null);
    }

    /**
     * 基本操作 删除
     * @param key
     * @param sharedPreferences
     */
    public  void removeBaseData(String key, SharedPreferences sharedPreferences){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.commit();
    }

    /**
     * 基本操作 清除
     * @param sharedPreferences
     */
    public void clearBaseData(SharedPreferences sharedPreferences) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear().commit();
    }

    public void setTrajectoryData(String key, String value) {
        setBaseData(key, value, trajectoryPreferences);
    }

    public String getTrajectoryData(String key) {
        return getBaseData(key,  trajectoryPreferences);
    }

    public void removeTrajectoryData( String key) {
        removeBaseData(key, trajectoryPreferences);
    }

    public void clearTrajectoryData( ) {
        trajectoryPreferences.edit().clear().commit();
    }

}
