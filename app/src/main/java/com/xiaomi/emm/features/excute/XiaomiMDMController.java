package com.xiaomi.emm.features.excute;

import android.app.ActivityManager;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver2;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.miui.enterprise.sdk.APNConfig;
import com.miui.enterprise.sdk.APNManager;
import com.miui.enterprise.sdk.ApplicationManager;
import com.miui.enterprise.sdk.DeviceManager;
//import com.miui.enterprise.sdk.PermissionManager;
import com.miui.enterprise.sdk.PhoneManager;
import com.miui.enterprise.sdk.RestrictionsManager;
import com.xiaomi.emm.base.EMMDeviceAdminReceiver;
import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.utils.TheTang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class XiaomiMDMController extends MDMController {
    private static final String TAG = XiaomiMDMController.class.getName();

    private APNManager mApnManager;
    private ApplicationManager mApplicationManager;
    private DeviceManager mDeviceManager;
//    private PermissionManager mPermissionManager;
    private PhoneManager mPhoneManager;
    private RestrictionsManager mRestrictionsManager;

    public static  DevicePolicyManager mDevicePolicyManager;
    public static ComponentName mComponentName;

//    private static int mUserId = 10;//10:安全域，0:生活域
    private static int mUserId = 0;//10:安全域，0:生活域

    Context mContext;

    //单例
    private volatile static XiaomiMDMController mMDMController;

    private XiaomiMDMController() {
    }

    public static XiaomiMDMController getSingleInstance() {
        if (null == mMDMController) {
            synchronized (XiaomiMDMController.class) {
                if (null == mMDMController) {
                    mMDMController = new XiaomiMDMController();
                }
            }
        }
        return mMDMController;
    }

    public void init(Context context) {
        mContext = context;

        mApnManager = APNManager.getInstance();
        mApplicationManager = ApplicationManager.getInstance();
        mDeviceManager = DeviceManager.getInstance();
        mPhoneManager = PhoneManager.getInstance();
        mRestrictionsManager = RestrictionsManager.getInstance();

        mDevicePolicyManager = (DevicePolicyManager) mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
        mComponentName = new ComponentName( mContext.getPackageName().toString(), EMMDeviceAdminReceiver.class.getName());
        mApplicationManager.setDeviceAdmin(mComponentName);
       boolean owner = mApplicationManager.setDeviceOwner(mComponentName, mUserId);
//       mApplicationManager.removeDeviceAdmin(mComponentName);
        setApplicationSettings();
    }

    private class MyPackageInstallObserver extends IPackageInstallObserver2.Stub {
        @Override
        public void onUserActionRequired(Intent intent) {
            // Ignore
        }

        @Override
        public void onPackageInstalled(String basePackageName, int returnCode, String msg, Bundle extras) {
            Log.d(TAG, "Install package:" + basePackageName + ", returnCode:" + returnCode + ", msg:" + msg);
            Toast.makeText(mContext, "Delete " + basePackageName + " returned " + returnCode, Toast.LENGTH_SHORT).show();
        }
    }

    private class MyPackageDeleteObserver extends IPackageDeleteObserver.Stub {
        @Override
        public void packageDeleted(String packageName, int returnCode) {
            Log.d(TAG, "Install package:" + packageName + ", returnCode:" + returnCode);
        }
    }

    /**
     * 静默安装apk
     * @param apkFilePath apk路径
     * @param flag 参阅Android标准API android.content.pm.PackageManager#installPackage中参数的InstallFlags说明
     * @param observer 应用安装回调，参阅Demo工程app下aidl与ApplicationManagerTestActivity相关实现
     */
    @Override
    public void installApplication(String apkFilePath, String installerPkg) {
        //flag参数设置
        //apkFilePath
        mApplicationManager.installPackage(apkFilePath, 2, new MyPackageInstallObserver());

    }

    /**
     * 静默卸载
     * @param packageName 应用包名
     * @param flag 参阅Android标准API android.content.pm.PackageManager#deletePackage中参数的DeleteFlags说明
     * @param observer 应用删除的回调，参阅Demo工程app下aidl与ApplicationManagerTestActivity相关实现
     */
    @Override
    public void uninstallApplication(String packageName) {
        mApplicationManager.deletePackage(packageName, 4 /* DELETE_SYSTEM_APP */,
                new MyPackageDeleteObserver());
    }

    @Override
    public boolean queryPkgNameFromUninstallList(String packageName) {
        int flag = mApplicationManager.getApplicationSettings(packageName);
        if ((flag & ApplicationManager.FLAG_PREVENT_UNINSTALLATION) == ApplicationManager.FLAG_PREVENT_UNINSTALLATION) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void addPkgNameToUninstallList(String packageName) {
        mApplicationManager.setApplicationSettings(packageName, ApplicationManager.FLAG_PREVENT_UNINSTALLATION);
    }

    @Override
    public boolean deletePkgNameFromUninstallList(String packageName) {
        int flag = mApplicationManager.getApplicationSettings(packageName);
        mApplicationManager.setApplicationSettings(packageName, flag & (~ApplicationManager.FLAG_PREVENT_UNINSTALLATION));
        return false;
    }

    @Override
    public boolean queryPkgNameFromInstallList(String packageName) {

        return false;
    }

    @Override
    public boolean addPkgNameToInstallList(String packageName) {
        return false;
    }

    @Override
    public boolean deletePkgNameFromInstallList(String packageName) {
        return false;
    }

    @Override
    public void disableAppAccessToNet(String packageName) {
        mApplicationManager.setMobileRestrict(packageName, true, mUserId);
        mApplicationManager.setWifiRestrict(packageName, true, mUserId);
    }

    @Override
    public void enableAppAccessToNet(String packageName) {
        mApplicationManager.setMobileRestrict(packageName, false, mUserId);
        mApplicationManager.setWifiRestrict(packageName, false, mUserId);
    }

    @Override
    public void switchContainer() {

    }

    @Override
    public void disableSwitching() {

    }

    @Override
    public void enableSwitching() {

    }

    @Override
    public boolean isInSecureContainer() {
        return false;
    }

    @Override
    public boolean isInFgContainer() {
        return true;
    }

    @Override
    public List<String> getDeviceInfo() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < Common.deviceInfo.length; i++) {

            if (Common.deviceInfo[i].equals("imei")) {
//                list.add(TheTang.getSingleInstance().getImei());
                list.add(getImei(0));
            } else if (Common.deviceInfo[i].equals("meid")) {
//                list.add(TheTang.getSingleInstance().getImei1());
                list.add(getImei(1));
            } else if (Common.deviceInfo[i].equals("run_memory")) {
                list.add(TheTang.getSingleInstance().getTotalRam());
            } else if (Common.deviceInfo[i].equals("phone_storage")) {
                long storage = (long) TheTang.getSingleInstance().getTotalStorage();
                list.add(TheTang.getSingleInstance().formatFileSize( storage ));
            } else if (Common.deviceInfo[i].equals("resolution")){
                list.add(TheTang.getSingleInstance().getResolution());
            } else if (Common.deviceInfo[i].equals("manufacturers")){
                list.add(TheTang.getSingleInstance().getManufacturers());
            } else if (Common.deviceInfo[i].equals("model")){
                list.add(TheTang.getSingleInstance().getModel());
            } else if (Common.deviceInfo[i].equals("android_version")){
                list.add(TheTang.getSingleInstance().getAndroidVersion());
            } else if (Common.deviceInfo[i].equals("system_version")){
                list.add(TheTang.getSingleInstance().getSystemVersion());
            } else if (Common.deviceInfo[i].equals("safe_version")){
                list.add("");
            } else if (Common.deviceInfo[i].equals("patch_level")){
                list.add("");
            } else if (Common.deviceInfo[i].equals("sim1_iccid")){
                list.add(TheTang.getSingleInstance().getIccid1());
            } else if (Common.deviceInfo[i].equals("sim2_iccid")){
                list.add(TheTang.getSingleInstance().getIccid2());
            } else if (Common.deviceInfo[i].equals("sim1_ismi")){
                list.add(TheTang.getSingleInstance().getSubscriberId1());
            } else if (Common.deviceInfo[i].equals("sim2_ismi")){
                list.add(TheTang.getSingleInstance().getSubscriberId2());
            }
        }
        return list;
    }

    @Override
    public void enableLocationService(boolean enable) {
//        LocationManager mLocationManager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
        //todo
        openGps(enable);
    }

    @Override
    public boolean isLocationServiceEnabled() {
        //todo
        return isGpsOpend();
//        return false;
    }

    @Override
    public boolean openGpsOnBGSlient() {
        return false;
    }

    @Override
    public boolean isGpsOpenedOnBGSlient() {
        return false;
    }

    @Override
    public void enableBluetooth(boolean enable) {
        if (enable) {
            mRestrictionsManager.setControlStatus(RestrictionsManager.BLUETOOTH_STATE, RestrictionsManager.ENABLE);
        } else {
            mRestrictionsManager.setControlStatus(RestrictionsManager.BLUETOOTH_STATE, RestrictionsManager.DISABLE);
        }
    }

    @Override
    public boolean isBluetoothEnabled() {
        int bluetoothState = mRestrictionsManager.getControlStatus(RestrictionsManager.BLUETOOTH_STATE);
        if (bluetoothState == RestrictionsManager.DISABLE) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void enableWifi(boolean enable) {
        if (enable) {
            mRestrictionsManager.setControlStatus(RestrictionsManager.WIFI_STATE, RestrictionsManager.ENABLE);
        } else {
            mRestrictionsManager.setControlStatus(RestrictionsManager.WIFI_STATE, RestrictionsManager.DISABLE);
        }
    }

    @Override
    public boolean isWifiEnabled() {
        int wifiState = mRestrictionsManager.getControlStatus(RestrictionsManager.WIFI_STATE);
        if (wifiState == RestrictionsManager.DISABLE) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void enableUsb(boolean enable) {
        mRestrictionsManager.setRestriction(RestrictionsManager.DISALLOW_MTP, !enable);
    }

    @Override
    public boolean isUsbEnabled() {
        return !mRestrictionsManager.hasRestriction(RestrictionsManager.DISALLOW_MTP);
    }

    @Override
    public void enableCamera(boolean enable) {
        mRestrictionsManager.setRestriction(RestrictionsManager.DISALLOW_CAMERA, !enable);
    }

    @Override
    public boolean isCameraEnabled() {
        return !mRestrictionsManager.hasRestriction(RestrictionsManager.DISALLOW_CAMERA);
    }

    @Override
    public void enableSoundRecording(boolean enable) {
        mRestrictionsManager.setRestriction(RestrictionsManager.DISALLOW_MICROPHONE, !enable);
    }

    @Override
    public boolean isSoundRecordingEnabled() {
        return !mRestrictionsManager.hasRestriction(RestrictionsManager.DISALLOW_MICROPHONE);
    }

    @Override
    public void enableNfc() {
        mRestrictionsManager.setControlStatus(RestrictionsManager.NFC_STATE, RestrictionsManager.ENABLE);
    }

    @Override
    public void disableNfc() {
        mRestrictionsManager.setControlStatus(RestrictionsManager.NFC_STATE, RestrictionsManager.DISABLE);
    }

    @Override
    public boolean isNfcEnabled() {
        int nfcState = mRestrictionsManager.getControlStatus(RestrictionsManager.NFC_STATE);
        if (nfcState == RestrictionsManager.DISABLE) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean createApn(ContentValues values) {
        return false;
    }

    @Override
    public void addApn(APNConfig config) {
        mApnManager.addAPN(config);
    }

    @Override
    public boolean deleteApn(String name) {
        return mApnManager.deleteAPN(name);
    }

    @Override
    public List<String> getApnList() {
        return null;
    }

    @Override
    public List<APNConfig> getAPNList() {
        return mApnManager.getAPNList();
    }

    @Override
    public ContentValues getApn(int id) {
        return null;
    }

    @Override
    public String getCurrentApn() {
        return null;
    }

    @Override
    public boolean setCurrentApn(int id) {
        return false;
    }

    @Override
    public void setScreenLock() {

    }

    @Override
    public void setPassword(String pwd) {
        setLockMethod(mDevicePolicyManager,mComponentName,pwd);
    }
    private static final byte[] TOKEN = "kNypAbZxbz6ZPw34YqB2z8ba0dYzJn2s".getBytes();//bai TOKEN勿动, todo save TOKEN
    private void setLockMethod(DevicePolicyManager mDevicePolicyManager, ComponentName mComponentName, String pwd) {
        Log.w( TAG, "setLockMethod!"+ pwd);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) {
            if (!mDevicePolicyManager.isResetPasswordTokenActive(mComponentName)) {
                mDevicePolicyManager.setResetPasswordToken(mComponentName, TOKEN);
            }
            boolean setPassword = mDevicePolicyManager.resetPasswordWithToken(mComponentName, pwd, TOKEN, DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
            if (!setPassword) {
                LogUtil.writeToFile(TAG, "set password failed!!!!!!!");
            }
            mDevicePolicyManager.lockNow();
        } else {
            try {
                if (pwd.isEmpty()) {
                    mDevicePolicyManager.setPasswordQuality(mComponentName, DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
                }
                mDevicePolicyManager.resetPassword(pwd, 0);
                mDevicePolicyManager.lockNow();

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void takeScreenShot() {
    }

    @Override
    public void setShutDown() {
        mDeviceManager.deviceShutDown();
    }

    @Override
    public void setReboot() {
        mDeviceManager.deviceReboot();
    }

    @Override
    public void setFactoryReset(boolean isWipeData) {
        mDeviceManager.recoveryFactory(isWipeData);//isWipeData: formatSD
    }

    @Override
    public void killProcess(String processName) {
        mApplicationManager.killProcess(processName);
    }

    @Override
    public void wipeData(boolean wipeTwoSystem, int flags) {

    }

    @Override
    public void enableSms(boolean enable) {
        if (enable) {
            mPhoneManager.controlSMS(PhoneManager.FLAG_DEFAULT);
        } else {
            mPhoneManager.controlSMS(PhoneManager.FLAG_DISALLOW_IN | PhoneManager.FLAG_DISALLOW_OUT);
        }
    }

    @Override
    public boolean isSmsEnabled() {
        if (mPhoneManager.getSMSStatus() == PhoneManager.FLAG_DEFAULT){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void enableTelephone(boolean enable) {
        if (enable) {
            mPhoneManager.controlPhoneCall(PhoneManager.FLAG_DEFAULT);
        } else {
            mPhoneManager.controlPhoneCall(PhoneManager.FLAG_DISALLOW_IN | PhoneManager.FLAG_DISALLOW_OUT);
        }
    }

    @Override
    public boolean isTelephoneEnabled() {
        if (mPhoneManager.getPhoneCallStatus() == PhoneManager.FLAG_DEFAULT){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean insertContact(String mName, String mNumber) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        if(getContactID(mName,mNumber) ) {
            Log.d(TAG, "contact already exist. exit.");
            LogUtil.writeToFile(TAG, "mName ="+mName+"mNumber"+ mNumber+ " ,contact already exist. exit.");
            return true;

        }

        if (mName.trim().equals("")){
            LogUtil.writeToFile(TAG, "mName ="+mName+"mNumber"+ mNumber+"contact name is empty. exit.");
            Log.d(TAG, "mName ="+mName+"mNumber"+ mNumber+"contact name is empty. exit.");
        }

        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .withYieldAllowed(true).build());

        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, mName)
                .build());
        Log.d(TAG, "add number: " + mName);
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, mNumber)
                .withValue( ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.Data.MIMETYPE)
                .build());
        Log.d(TAG, "add number: " + mNumber);

        try {
            mContext.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            Log.d(TAG, "add contact success.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "add contact failed.");
            LogUtil.writeToFile(TAG, "add contact failed.RemoteException == " + e.toString());
        }
        return true;
    }

    public boolean getContactID(String name,String number) {
        String id = "0";

        Cursor cursor = mContext.getContentResolver().query(
                android.provider.ContactsContract.Contacts.CONTENT_URI,
                new String[]{android.provider.ContactsContract.Contacts._ID},
                android.provider.ContactsContract.Contacts.DISPLAY_NAME +
                        "='" + name + "'", null, null);
       /* if(cursor.moveToNext()) {
            id = cursor.getString(cursor.getColumnIndex(
                    android.provider.ContactsContract.Contacts._ID));
        }*/

        if (cursor == null) {
            return false;
        }

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            id = cursor.getString(cursor.getColumnIndex(android.provider.ContactsContract.Contacts._ID));

            Cursor phoneCursor = mContext.getContentResolver()
                    .query(ContactsContract.CommonDataKinds.
                                    Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.
                                    Phone.CONTACT_ID + "=" + "?",
                            new String[]{String.valueOf(id)},
                            null);

            if (phoneCursor == null) {
                return false;
            }

            for (phoneCursor.moveToFirst(); !phoneCursor.isAfterLast(); phoneCursor.moveToNext()) {

                String   phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                Log.w(TAG,"查询数据库----phoneNumber =" +phoneNumber +" name ="+name);
                LogUtil.writeToFile(TAG,"查询数据库----phoneNumber =" +phoneNumber+" name ="+name);
                if (number.equals(phoneNumber )){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean deleteContactByName(String mName) {
        return false;
    }

    /**
     * delete contact
     *
     * @param mName
     * @param mNumber
     */
    public void deleteContact(String mName, String mNumber) {

        Cursor cursor = mContext.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, new String[]{ContactsContract.Data._ID},
                "display_name=?", new String[]{mName}, null);

        String phoneNumber = null;

        if (cursor == null) {
            return;
        }

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

            int id = cursor.getInt(0);

            Cursor phoneCursor = mContext.getContentResolver().query(ContactsContract.CommonDataKinds.
                    Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.
                    Phone.CONTACT_ID + "=" + "?", new String[]{String.valueOf(id)}, null);

            if (phoneCursor == null) {
                return;
            }

            for (phoneCursor.moveToFirst(); !phoneCursor.isAfterLast(); phoneCursor.moveToNext()) {

                phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                LogUtil.writeToFile(TAG, "phoneNumber1 = " + phoneNumber);
                if (mNumber.equals(phoneNumber)) {

                    ArrayList<ContentProviderOperation> ops = new ArrayList<>();

                    ops.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                            .withSelection(ContactsContract.RawContacts.CONTACT_ID + "=" + id, null)
                            .build());

                    ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                            .withSelection(ContactsContract.Data.CONTACT_ID + "=" + id, null)
                            .build());
                    try {
                        mContext.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    } catch (OperationApplicationException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 更新联系人
     *
     * @param mName
     * @param oldNumber
     * @param newNumber
     */
    public void updateContact(String mName, String oldNumber, String newNumber) {

        Cursor cursor = mContext.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, new String[]{ContactsContract.Data._ID},
                "display_name=?", new String[]{mName}, null);

        String phoneNumber = null;
        Log.w(TAG,"updateContact : mName="+mName +" oldNumber="+oldNumber +"newNumber= "+newNumber);

        if (cursor == null) {
            return;
        }

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String id = "0";
            //  int id = cursor.getInt(0);
            id = cursor.getString(cursor.getColumnIndex(android.provider.ContactsContract.Contacts._ID));
            Log.w(TAG,"updateContact id= "+id);
            Cursor phoneCursor = mContext.getContentResolver().query(ContactsContract.CommonDataKinds.
                    Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.
                    Phone.CONTACT_ID + "=" + "?", new String[]{String.valueOf(id)}, null);

            if (phoneCursor == null) {
                return;
            }

            for (phoneCursor.moveToFirst(); !phoneCursor.isAfterLast(); phoneCursor.moveToNext()) {

                phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                Log.w(TAG,"查询数据库==oldNumber ="+oldNumber +" phoneNumber =" +phoneNumber);
                if (oldNumber.equals(phoneNumber)) {
                    Log.w(TAG,"查询数据库");
                    ArrayList<ContentProviderOperation> ops = new ArrayList<>();

                   /* ops.add(ContentProviderOperation.newUpdate(ContactsContract.RawContacts.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, newNumber)
                            .build());*/

//                    ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
//                            .withSelection(ContactsContract.Data.CONTACT_ID + "=" + id, null)
//                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, newNumber)
//                            .build());

                    //update number
                    if(!newNumber.trim().equals("")) {
                        ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                                .withSelection( ContactsContract.Data.CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + "=?",
                                        new String[]{id, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE})
                                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, newNumber)
                                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.Data.MIMETYPE)
                                .build());
                        Log.d(TAG, "update number: " + newNumber.trim());
                    }
                    try {
                        mContext.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    } catch (OperationApplicationException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public String getAllContactInfo() {
        return null;
    }

    @Override
    public Uri getActualDefaultRingtoneUri(Context context, int type) {
        return null;
    }

    @Override
    public boolean queryMacFromBTSocketList(String deviceMac) {
        return false;
    }

    @Override
    public boolean addMacToBTSocketList(String deviceMac) {
        return false;
    }

    @Override
    public boolean deleteMacFromBTSocketList(String deviceMac) {
        return false;
    }

    @Override
    public boolean enableBluetoothOpp(boolean enable) {
        return false;
    }

    @Override
    public boolean isBluetoothOppEnabled() {
        return false;
    }

    @Override
    public void openWifiOnBG(boolean open) {
        if (open) {
            mRestrictionsManager.setControlStatus(RestrictionsManager.WIFI_STATE, RestrictionsManager.FORCE_OPEN);
        } else {
            mRestrictionsManager.setControlStatus(RestrictionsManager.WIFI_STATE, RestrictionsManager.CLOSE);
        }
    }

    @Override
    public boolean isWifiOpened() {
        return false;
    }

    @Override
    public boolean queryMacFromWifiList(String deviceMac) {
        return false;
    }

    @Override
    public boolean addMacToWifiList(String deviceMac) {
        return false;
    }

    @Override
    public boolean deleteMacFromWifiList(String deviceMac) {
        return false;
    }

    @Override
    public void enableWifiAP(boolean enable) {
        mRestrictionsManager.setRestriction(RestrictionsManager.DISALLOW_TETHER, !enable);
    }

    @Override
    public boolean isWifiAPEnabled() {
        return !mRestrictionsManager.hasRestriction(RestrictionsManager.DISALLOW_TETHER);
    }

    @Override
    public boolean queryMacFromWifiAPList(String deviceMac) {
        return false;
    }

    @Override
    public boolean addMacToWifiAPList(String deviceMac) {
        return false;
    }

    @Override
    public boolean deleteMacFromWifiAPList(String deviceMac) {
        return false;
    }

    @Override
    public boolean setWifiApOpened(WifiConfiguration wifiConfig, boolean opened) {
        return false;
    }

    @Override
    public int getWifiApState() {
        return 0;
    }

    @Override
    public boolean isWifiAPOpened() {
        return false;
    }

    @Override
    public WifiConfiguration getWifiApConfiguration() {
        return null;
    }

    @Override
    public boolean setWifiApConfiguration(WifiConfiguration wifiConfig) {
        return false;
    }

    @Override
    public boolean openNfc() {
        return false;
    }

    @Override
    public boolean closeNfc() {
        return false;
    }

    @Override
    public boolean enableScreenShot() {
        mRestrictionsManager.setRestriction(RestrictionsManager.DISALLOW_SCREENCAPTURE, false);
        return false;
    }

    @Override
    public boolean disableScreenShot() {
        mRestrictionsManager.setRestriction(RestrictionsManager.DISALLOW_SCREENCAPTURE, true);
        return false;
    }

    @Override
    public boolean isScreenShotEnabled() {
        return !mRestrictionsManager.hasRestriction(RestrictionsManager.DISALLOW_SCREENCAPTURE);
    }

    @Override
    public boolean enableDropdown() {
        //todo bai test
        mDeviceManager.disableStatusBarAndButton(DeviceManager.DISABLE_NONE);
        return true;
    }

    @Override
    public boolean disableDropdown() {
        mDeviceManager.disableStatusBarAndButton(DeviceManager.DISABLE_EXPAND);
        return true;
    }

    @Override
    public boolean isDropdownEnabled() {
        //todo baii
        return true;
    }

    @Override
    public void enableReset() {
        mRestrictionsManager.setRestriction(RestrictionsManager.DISALLOW_FACTORYRESET, false);
    }

    @Override
    public void disableReset() {
        mRestrictionsManager.setRestriction(RestrictionsManager.DISALLOW_FACTORYRESET, true);
    }

    @Override
    public boolean isResetEnabled() {
        return !mRestrictionsManager.hasRestriction(RestrictionsManager.DISALLOW_FACTORYRESET);
    }

    @Override
    public void enableModifySystemTime() {
        mRestrictionsManager.setRestriction(RestrictionsManager.DISALLOW_TIMESET, false);
    }

    @Override
    public void disableModifySystemTime() {
        mRestrictionsManager.setRestriction(RestrictionsManager.DISALLOW_TIMESET, true);
    }

    @Override
    public boolean isModifySystemTimeEnabled() {
        return mRestrictionsManager.hasRestriction(RestrictionsManager.DISALLOW_TIMESET);
    }

    @Override
    public boolean enableSD() {
        return false;
    }

    @Override
    public boolean disableSD() {
        return false;
    }

    @Override
    public boolean setAppInstallationPolicy(HashMap<String, String> policy) {
        return false;
    }

    @Override
    public HashMap<String, String> getAppInstallationPolicy() {
        return null;
    }

    @Override
    public void enableUninstallWhiteListFunction(boolean enable) {

    }

    @Override
    public void enableInstallWhiteListFunction(boolean enable) {

    }

    @Override
    public boolean queryBTWhiteList(String BTDevice) {
        return false;
    }

    @Override
    public List<String> queryAllBTWhiteList() {
        return null;
    }

    @Override
    public boolean addDeviceToBTWhiteList(String BTDevice) {
        return false;
    }

    @Override
    public boolean deleteDeviceToBTWhiteList(String BTDevice) {
        return false;
    }

    @Override
    public boolean openWifiOnBGSlient() {
        return false;
    }

    @Override
    public boolean isWifiOpenedOnBGSlient() {
        return false;
    }

    @Override
    public boolean forceLocationService(boolean isForceOpenLocation) {
        openGps(isForceOpenLocation);
        return true;
    }

    @Override
    public boolean enableSecSimcard(boolean enable) {
        return false;
    }

    @Override
    public boolean isSecSimcardEnabled() {
        return false;
    }

    @Override
    public boolean setKeyVisible(boolean visible) {
        return false;
    }

    @Override
    public boolean setHomeKeyVisible(boolean visible) {
        return false;
    }

    @Override
    public boolean setRecentKeyVisible(boolean visible) {
        if (visible) {
            mDeviceManager.disableStatusBarAndButton(DeviceManager.DISABLE_NONE);
        } else {
            mDeviceManager.disableStatusBarAndButton(DeviceManager.DISABLE_RECENT | DeviceManager.DISABLE_EXPAND);
        }
        return true;
    }

    @Override
    public boolean enableFingerNavigation(boolean enable) {
        return false;
    }

    /**
     * controlCellular,flag 0：启用，1：禁用，2：关闭（在启用的前提下） ，3 ：开启（在启用的前提下）， 4：强制开启
     *
     * @param isOpen
     * @return
     */
    @Override
    public void openDataConnectivity(boolean isOpen) {
        mPhoneManager.controlCellular(4);
    }

    @Override
    public boolean isDataConnectivityOpen() {
        int dataStatus0 = mPhoneManager.getCellularStatus();
        if (dataStatus0 == 3  || dataStatus0 == 4) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean enable4G(int mode) {
        return false;
    }

    @Override
    public boolean is4GOpen() {
        return false;
    }

    @Override
    public void openGps(boolean isSetOpen) {
        if (isSetOpen) {
            mRestrictionsManager.setControlStatus(RestrictionsManager.GPS_STATE, RestrictionsManager.FORCE_OPEN);
        } else {
            mRestrictionsManager.setControlStatus(RestrictionsManager.GPS_STATE, RestrictionsManager.CLOSE);
        }
    }

    @Override
    public boolean isGpsOpend() {
        int gpsState = mRestrictionsManager.getControlStatus(RestrictionsManager.GPS_STATE);
        if (gpsState == RestrictionsManager.DISABLE) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean setLocationPolicy(int policy) {
        return false;
    }

    @Override
    public int getLocationPolicy() {
        return 0;
    }

    @Override
    public boolean forceLockScreen() {
        return false;
    }

    @Override
    public Boolean releaseLockScreen() {
        return null;
    }

    @Override
    public boolean setPasswordWithPolicy(String pwd, int policy) {
        return false;
    }

    @Override
    public boolean setPasswordNone() {
        setLockMethod(mDevicePolicyManager,mComponentName,"");
        return true;
    }

    @Override
    public String executeShellToSetIptables(String shellCommand) {
        return mDeviceManager.executeShellToSetIptables(shellCommand);
    }

    @Override
    public boolean queryIACList(String iacItem) {
        return false;
    }

    @Override
    public List<String> queryAllIACList() {
        return null;
    }

    @Override
    public boolean addItemToIACList(String iacItem) {
        return false;
    }

    @Override
    public boolean deleteItemFromIACList(String iacItem) {
        return false;
    }

    @Override
    public List<String[]> getAppPowerUsage() {
        return null;
    }

    @Override
    public List<String[]> getRunningApplication() {
        return null;
    }

    @Override
    public String[] getAppTrafficInfo(int uid) {
        return new String[0];
    }

    private String getImei(int slotId) {
        return mPhoneManager.getIMEI(slotId);
    }

    @Override
    public void setAccessibilityService(ComponentName componentName, boolean isActive) {
        mApplicationManager.enableAccessibilityService(componentName, isActive);
    }


    @Override
    public boolean isAccessibilityServiceEnable(ComponentName componentName) {
        //no such method in xiaomi
        return false;
    }

    @Override
    public void setHome(String pkgName) {
        mDeviceManager.setDefaultHome(pkgName);
    }

    public void setApplicationSettings() {
        int flag = ApplicationManager.FLAG_KEEP_ALIVE |
//                ApplicationManager.FLAG_PREVENT_UNINSTALLATION |
                ApplicationManager.FLAG_GRANT_ALL_RUNTIME_PERMISSION | //会导致PACKAGE_USAGE_STATS权限置灰,如果之前是关闭状态则无法打开
                ApplicationManager.FLAG_ALLOW_AUTOSTART;
        mApplicationManager.setApplicationSettings(mContext.getPackageName(), flag);
    }

    //设置应用黑名单
    public void setApplicationBlackList(List<String> packages) {
        mApplicationManager.setApplicationRestriction(ApplicationManager.RESTRICTION_MODE_BLACK_LIST);
        mApplicationManager.setApplicationBlackList(packages);
    }

    //设置应用白名单
    public void setApplicationWhiteList(List<String> packages) {
        mApplicationManager.setApplicationRestriction(ApplicationManager.RESTRICTION_MODE_WHITE_LIST);
        mApplicationManager.setApplicationWhiteList(packages);
    }

    //清除应用黑白名单
    public void setApplicationDefault() {
        mApplicationManager.setApplicationRestriction(ApplicationManager.RESTRICTION_MODE_DEFAULT);
    }

    @Override
    public void setCallWhiteList(List<String> callWhiteList) {
        mPhoneManager.setCallWhiteList(callWhiteList);
    }

    @Override
    public boolean isCallWhiteListOpen() {
        return PhoneManager.getInstance().getCallContactRestriction() == PhoneManager.RESTRICTION_MODE_WHITE_LIST;
    }

    @Override
    public void setCallWhiteList(boolean open) {
        if (open) {
            mPhoneManager.setCallContactRestriction(PhoneManager.RESTRICTION_MODE_WHITE_LIST);
        } else {
            mPhoneManager.setCallContactRestriction(PhoneManager.RESTRICTION_MODE_DEFAULT);
        }
    }

    @Override
    public void setCallAutoRecord(boolean open) {
        mPhoneManager.setPhoneCallAutoRecord(open);
    }

    @Override
    public void setCallAutoRecordDir(String path) {
        mPhoneManager.setPhoneCallAutoRecordDir(path);
    }

    @Override
    public boolean isCallAutoRecord() {
        return mPhoneManager.isAutoRecordPhoneCall();
    }
}