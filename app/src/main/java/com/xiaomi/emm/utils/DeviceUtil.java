package com.xiaomi.emm.utils;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.features.db.DatabaseOperate;
import com.xiaomi.emm.model.APPInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static android.content.Context.BATTERY_SERVICE;
import static android.content.Context.TELEPHONY_SERVICE;

/**
 * Created by Administrator on 2017/9/4.
 */

public class DeviceUtil {
    public final static String TAG = "DeviceUtil";

    static JSONObject deviceObject = new JSONObject();

    public String getDeviceInfo() {
        return deviceObject.toString();
    }

    public static final class Builder {

        TelephonyManager mTelephonyManager = null;

        public Builder() {
            mTelephonyManager = (TelephonyManager) TheTang.getSingleInstance().getContext().getSystemService( TELEPHONY_SERVICE );
        }

        //private String main_imei = null; //主 imei
        //private String vice_imei = null; //副卡 imei
        private String rom_available_capacity = null; //ROM可用容量
        private String rom_total_capacity = null; //ROM总容量
        //private String device_manufacturer = null; //'设备厂商
        //private String device_type = null; //设备类型
        //private String current_system_version = null; //当前操作系统版本
        //private String main_iccid = null; //主iccid
        //private String vice_iccid = null; //副iccid
        //private String main_imsi = null; //主卡 imsi
        //private String vice_imsi = null; //副卡 imsi


        //private String user_id = null; //用户ID
        private String device_name = null; //设备名称

        //private String activation_time = null; //激活时间
        //private String recent_online = null; //最近上线时间

        private String client_last_update_time = null; //客户端上次更新新时间
        private String app_security_password = null; //app密保密码

        private String cpu = null; //cpu型号
        private String sd_card_serial_number = null; //SD卡序列号
        private String udid = null; //唯一标识符
        private String wifi_mac = null; //WiFi MAC地址

        private String power_status = null; //电量状态
        private int is_root = 0; //是否Rooted/越狱 0 否 1是
        private String sd_card_total_capacity = null; //SD卡容量 单位GB
        private String sd_card_available_capacity = null; //SD卡可用容量
        private String ram = null; //RAM
        private String camera = null; //相机像素

        private String device_model = null; //设备型号
        private String boot_time = null; //开机时长

        private String bluetooth_mac_address = null; //蓝牙MAC地址

        private String main_operator_info = null; //主运营商信息
        private String vice_operator_info = null; //副运营商信息

        private String main_phone_number = null; //手机号码
        private String vice_phone_number = null; //副卡手机号码
        /*-private String main_phone_number = null; //手机号码
        private String main_is_open_roam = null; //是否开启漫游
        private String main_total_flow = null; //总流量 单位MB
        private String main_mobile_flow_day = null; //移动数据流量 天
        private String main_mobile_flow_week = null; //移动数据流量 周
        private String main_mobile_flow_month = null; //'移动数据流量 月
        private String wifi_flow_day = null; //无线网络流量(MB) 当天
        private String wifi_flow_week = null; //无线网络流量(MB) 每周
        private String wifi_flow_month = null; //无线网络流量(MB) 没月

        private String vice_phone_number = null; //副卡手机号码
        private String vice_is_open_roam = null; //副卡是否开启漫游
        private String vice_mobile_flow_day = null; //'副卡移动数据流量天
        private String vice_mobile_flow_week = null; //副卡 移动数据流量 周
        private String vice_mobile_flow_month = null; //副卡 移动数据流量 月

        private String is_new_data = null; //副卡是否开启漫游*/

        /*apps:[

        { `name` varchar(50) DEFAULT NULL,// 名称
            `version` varchar(50) DEFAULT '',// 版本
            `size` varchar(50) DEFAULT NULL,// 大小
            `package_name` varchar(50) DEFAULT '',// 包名
            `last_update_time` datetime DEFAULT NULL,// 最后更新时间
            ]*/

        public Builder getDeviceType() {
            addToObject( "device_type", 0 );
            return this;
        }

        public Builder getAlias() {
            addToObject( Common.alias, PreferencesManager.getSingleInstance().getData( Common.alias ) );
            return this;
        }

        /**
         * 获得设备名称
         *
         * @return
         */
        public Builder getDeviceName() {
            this.device_name = Build.DEVICE;
            addToObject( "device_name", device_name );
            return this;
        }

        /**
         * 获得上次客户端安装时间
         *
         * @return
         */
        public Builder getClientLastUpdateTime() {
            this.client_last_update_time = PreferencesManager.getSingleInstance().getOtherData( "client_last_update_time" );
            addToObject( "client_last_update_time", client_last_update_time );
            return this;
        }

        /**
         * 获得应用保护密码
         *
         * @return
         */
        public Builder getAppSecurityPassword() {
            this.app_security_password = PreferencesManager.getSingleInstance().getLockPassword( "password" );
            addToObject( "app_security_password", app_security_password );
            return this;
        }

        /**
         * 获得cpu型号
         *
         * @return
         */
        public Builder getCpu() {
            this.cpu = android.os.Build.CPU_ABI;
            addToObject( "cpu", cpu );
            return this;
        }

        /**
         * 获得SD卡序列号
         *
         * @return
         */
        public Builder getSdCardSerialNumber() {
            // 串号/序列号
            /*FileReader localOb = null;
            try {
                localOb = new FileReader( "/sys/block/mmcblk1/device/serial" );//不同的手机可能不一样
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            //如果未插卡，返回null
            if (localOb != null) {

                String sd_serial = null;
                try {
                    sd_serial = new BufferedReader( (Reader) localOb ).readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                this.sd_card_serial_number = sd_serial;
                addToObject( "sd_card_serial_number", sd_card_serial_number );
            }*/

            sd_card_serial_number = TheTang.getSingleInstance().getSDCardId();

            addToObject( "sd_card_serial_number", sd_card_serial_number );
            return this;
        }

        /**
         * 获得唯一标识符
         *
         * @return
         */
        public Builder getUDID() {
            if (PreferencesManager.getSingleInstance().getData( "udid" ) == null) {
                this.udid = UUIDGenerator.getUUID();
            } else {
                this.udid = PreferencesManager.getSingleInstance().getData( "udid" );
            }
            addToObject( "udid", udid );
            return this;
        }

        /**
         * 获得wifi mac
         *
         * @return
         */
        public Builder getWifiMac() {
            this.wifi_mac = getMacAddr();
            addToObject( "wifi_mac", wifi_mac );
            return this;
        }

        private static String byte2hex(byte[] b, int length) {
            StringBuffer hs = new StringBuffer( length );
            String stmp = "";
            int len = length;
            for (int n = 0; n < len; n++) {
                stmp = Integer.toHexString( b[n] & 0xFF );
                if (stmp.length() == 1) {
                    hs = hs.append( "0" ).append( stmp );
                } else {
                    hs = hs.append( stmp );
                }
                if (n != len - 1) {
                    hs.append( ":" );
                }
            }
            return String.valueOf( hs );
        }

        /**
         * 获得剩余手机电量
         *
         * @return
         */
        public Builder getPowerStatus() {
            BatteryManager batteryManager = (BatteryManager) TheTang.getSingleInstance().getContext().getSystemService( BATTERY_SERVICE );
            this.power_status = String.valueOf( batteryManager.getIntProperty( BatteryManager.BATTERY_PROPERTY_CAPACITY ) );
            addToObject( "power_status", power_status + "%" );
            return this;
        }

        /**
         * 获取手机是否越狱
         *
         * @return
         */
        public Builder getIsRoot() {
            if (Root.isDeviceRooted()) {
                this.is_root = 1;
            }

            addToObject( "is_root", is_root );
            return this;
        }

        /**
         * 获得sd卡总容量
         *
         * @return
         */
        public Builder getSdCardTotalCapacity() {

            boolean sdCardExist = Environment.getExternalStorageState().equals( android.os.Environment.MEDIA_MOUNTED ); //判断sd卡是否挂载
            if (!sdCardExist) {
                this.sd_card_total_capacity = "";
                return this;
            }

            long storage = TheTang.getSingleInstance().getTotalSpace( "/mnt/ext_sdcard" );
            if (storage == 0) {
                this.sd_card_total_capacity = "";
            } else {
                this.sd_card_total_capacity = TheTang.getSingleInstance().formatFileSize( storage );
            }
            addToObject( "sd_card_total_capacity", sd_card_total_capacity );
            return this;
        }

        /**
         * 获得sd卡剩余容量
         *
         * @return
         */
        public Builder getSdCardAvailableCapacity() {

            boolean sdCardExist = Environment.getExternalStorageState().equals( android.os.Environment.MEDIA_MOUNTED ); //判断sd卡是否挂载
            if (!sdCardExist) {
                this.sd_card_available_capacity = "";
                return this;
            }

            long storage = TheTang.getSingleInstance().getAvailSpace( "/mnt/ext_sdcard" );
            if (storage == 0) {
                this.sd_card_available_capacity = "";
            } else {
                this.sd_card_available_capacity = TheTang.getSingleInstance().formatFileSize( storage );
            }
            addToObject( "sd_card_available_capacity", sd_card_available_capacity );
            return this;
        }

        /**
         * 获取Rom总容量
         *
         * @return
         */
        public Builder getRomTotal() {
            long storage = (long) TheTang.getSingleInstance().getTotalStorage();
            this.rom_total_capacity = TheTang.getSingleInstance().formatFileSize( storage );
            addToObject( "rom_total_capacity", rom_total_capacity );
            return this;
        }

        /**
         * 获取Rom剩余容量
         *
         * @return
         */
        public Builder getRomAvailable() {
            long storage = (long) TheTang.getSingleInstance().getRemainStorage();
            this.rom_available_capacity = TheTang.getSingleInstance().formatFileSize( storage );
            addToObject( "rom_available_capacity", rom_available_capacity );
            return this;
        }

        /**
         * 获得相机像素
         *
         * @return
         */
        public Builder getCamera() {
            this.camera = getCameraPixels( 0 );//根据不同的摄像头
            addToObject( "camera", camera );
            return this;
        }

        /**
         * 相机像素
         *
         * @param paramInt
         * @return
         */
        public static String getCameraPixels(int paramInt) {
            Camera localCamera = null;

            String camera = PreferencesManager.getSingleInstance().getData( "camera" );
            if (camera != null) {
                return camera;
            }

            try {
                localCamera = Camera.open( paramInt ); //在相机disable的情况下 会抛出异常
            } catch (Exception e) {
                return null;
            }

            Camera.Parameters localParameters = localCamera.getParameters();
            localParameters.set( "camera-id", 1 );
            List<Size> localList = localParameters.getSupportedPictureSizes();
            if (localList != null) {
                int heights[] = new int[localList.size()];
                int widths[] = new int[localList.size()];
                for (int i = 0; i < localList.size(); i++) {
                    Camera.Size size = (Size) localList.get( i );
                    int sizehieght = size.height;
                    int sizewidth = size.width;
                    heights[i] = sizehieght;
                    widths[i] = sizewidth;
                }
                int pixels = getMaxNumber( heights ) * getMaxNumber( widths );
                localCamera.release();
                PreferencesManager.getSingleInstance().setOtherData( "camera", String.valueOf( pixels / 10000 ) + " 万" );
                return String.valueOf( pixels / 10000 ) + " 万";
            }
            return null;
        }

        public static int getMaxNumber(int[] paramArray) {
            int temp = paramArray[0];
            for (int i = 0; i < paramArray.length; i++) {
                if (temp < paramArray[i]) {
                    temp = paramArray[i];
                }
            }
            return temp;
        }

        /**
         * 获得设备型号
         *
         * @return
         */
        public Builder getDeviceModel() {
            this.device_model = Build.MODEL;
            addToObject( "device_model", device_model );
            return this;
        }

        /**
         * 获得开机时长
         *
         * @return
         */
        public Builder getBootTime() {
            this.boot_time = TheTang.getSingleInstance().formatTimeLength( SystemClock.elapsedRealtime() );
            addToObject( "boot_time", boot_time );
            return this;
        }

        /**
         * 获得蓝牙MAC地址
         *
         * @return
         */
        public Builder getBluetoothMacAddress() {
            /*try {
                Class classz = Class.forName("android.bluetooth.BluetoothDevice");
                Constructor con = classz.getDeclaredConstructor(String.class);
                con.setAccessible(true);
                BluetoothDevice bluetoothDevice = (BluetoothDevice)con.newInstance(null);
                this.bluetooth_mac_address = bluetoothDevice.getAddress();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }*/

            addToObject( "bluetooth_mac_address", bluetooth_mac_address );
            return this;
        }

        /**
         * 获得Wifi MAC地址
         *
         * @return
         */
        public static String getMacAddr() {
            try {
                List<NetworkInterface> all = Collections.list( NetworkInterface.getNetworkInterfaces() );
                for (NetworkInterface nif : all) {
                    if (!"wlan0".equalsIgnoreCase( nif.getName() )) {
                        continue;
                    }

                    byte[] macBytes = nif.getHardwareAddress();
                    if (macBytes == null) {
                        return "";
                    }

                    StringBuilder res1 = new StringBuilder();
                    for (byte b : macBytes) {
                        res1.append( String.format( "%02X:", b ) );
                    }

                    if (res1.length() > 0) {
                        res1.deleteCharAt( res1.length() - 1 );
                    }
                    return res1.toString();
                }
            } catch (Exception ex) {
            }
            return "02:00:00:00:00:00";
        }

        /**
         * 获得系统硬件信息
         *
         * @return
         */
        public Builder getSystem() {
            List<String> deviceInfo = MDM.getDeviceInfo();

            if (deviceInfo != null && deviceInfo.size() > 0) {
                for (int i = 0; i < (deviceInfo.size() >= Common.deviceInfo1.length ? Common.deviceInfo1.length : deviceInfo.size()); i++) {
                    addToObject( Common.deviceInfo1[i], deviceInfo.get( i ) );
                }
            }
            return this;
        }

        /**
         * 获得系统App
         *
         * @return
         */
        public Builder getAllSystemApp() {

            //获取Launcher Apps
            List<LauncherActivityInfo> appList = TheTang.getSingleInstance().getLauncherApps();

            List<LauncherActivityInfo> newAppList = new ArrayList<>();
            //去掉重复item
            newAppList = TheTang.getSingleInstance().removeDuplicateWithOrder( appList );

            JSONArray appArray = new JSONArray();
            PackageManager mPackageManager = TheTang.getSingleInstance().getPackageManager();

            for (LauncherActivityInfo launcherActivityInfo : newAppList) {
                JSONObject appObject = new JSONObject();
                try {
                    String packageName = launcherActivityInfo.getApplicationInfo().packageName;

                    PackageInfo packageInfo = null;
                    try {
                        packageInfo = mPackageManager.getPackageInfo( packageName, 0 );
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                    appObject.put( "version", packageInfo.versionName );

                    try {
                        appObject.put( "size", TheTang.getSingleInstance().queryPackageSize( packageName ) );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    appObject.put( "name", packageInfo.applicationInfo.loadLabel( mPackageManager ) );
                    appObject.put( "package_name", packageName );

                    APPInfo appInfo = DatabaseOperate.getSingleInstance().queryAppInfo( packageName );

                    if (appInfo != null) {
                        appObject.put( "appId", appInfo.getAppId() );
                    } else {
                        appObject.put( "appId", "" );
                    }

                    appObject.put( "last_update_time", packageInfo.lastUpdateTime );

                    /**
                     * 设置当前应用为系统应用
                     */
                    if (Common.packageName.equals( packageName )) {
                        appObject.put( "type", "0" );
                        appArray.put( appObject );
                        continue;
                    }
                    /**
                     * 用于标识应用的类型
                     * 0：系统应用
                     * 1：商店应用
                     * 2：用户安装应用
                     */
                    if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                        appObject.put( "type", "0" );
                    } else {
                        if (DatabaseOperate.getSingleInstance().queryAppInfo( packageName ) != null) {
                            appObject.put( "type", "1" );
                        } else {
                            appObject.put( "type", "2" );
                        }
                    }

                    appArray.put( appObject );
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            try {
                deviceObject.put( "apps", appArray );
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return this;
        }

        /**
         * 获得运营商名称
         *
         * @return
         */
        public Builder getOperaterAbout() {

            String[] imsis = TheTang.getSingleInstance().getSubscriberId();
            String[] operaters = new String[2];
            if (imsis != null && imsis.length > 0) {
                for (int i = 0; i < imsis.length; i++) {
                    if (imsis[i] != null) {
                        if (imsis[i].startsWith( "46000" ) || imsis[i].startsWith( "46002" ) || imsis[i].startsWith( "46004" )
                                || imsis[i].startsWith( "46007" ) || imsis[i].startsWith( "46008" )) {
                            operaters[i] = "中国移动";
                        } else if (imsis[i].startsWith( "46001" ) || imsis[i].startsWith( "46006" ) || imsis[i].startsWith( "46009" ) || imsis[i].startsWith( "46010" )) {
                            operaters[i] = "中国联通";
                        } else if (imsis[i].startsWith( "46003" ) || imsis[i].startsWith( "46005" ) || imsis[i].startsWith( "46011" )) {
                            operaters[i] = "中国电信";
                        }
                    }
                }
            }
            this.main_operator_info = operaters[0];
            this.vice_operator_info = operaters[1];
            addToObject( "main_operator_info", main_operator_info );
            addToObject( "vice_operator_info", vice_operator_info );
            return this;
        }

        /**
         * 获得电话号码
         *
         * @return
         */
        public Builder getLine1Number() {
            String[] nums = TheTang.getSingleInstance().getLine1Number();
            this.main_phone_number = nums[0];
            this.vice_phone_number = nums[1];
            addToObject( "main_phone_number", main_phone_number );
            addToObject( "vice_phone_number", vice_phone_number );
            return this;
        }

        /**
         * 获得流量
         *
         * @return
         */
//        String mobileData;
        public Builder getMobileData() {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    String mobileData = NetworkStatsHelper.getFlowData();//querySummaryForDevice, must be used in main thread for xiaomi

                    if (mobileData != null) {
                        String[] datas = mobileData.split( "," );
                        for (int i = 0; i < datas.length; i++) {
                            addToObject( Common.mobileDatas[i], datas[i] );
                        }
                    }
                }
            });
            return this;
        }

        /**
         * 创建对象
         *
         * @return
         */
        public DeviceUtil build() {
            return new DeviceUtil();
        }
    }

    /**
     * 添加数据到JSONObject
     *
     * @param key
     * @param value
     */
    private static void addToObject(String key, Object value) {
        try {
            deviceObject.put( key, value );
        } catch (JSONException e) {
            LogUtil.writeToFile( TAG, e.toString() );
            e.printStackTrace();
        }
    }
}
