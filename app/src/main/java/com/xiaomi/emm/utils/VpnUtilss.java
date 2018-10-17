package com.xiaomi.emm.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by lenovo on 2017/12/25.
 */

public class VpnUtilss {//todo baii util ???
    private static final String TAG = "VpnUtilss";
    public static Class vpnProfileClz;
    private static Class credentialsClz;
    private static Class keyStoreClz;
    private static Class iConManagerClz;
    private static Object iConManagerObj;

    /**
     * 使用其他方法前先调用该方法
     * 初始化vpn相关的类
     * @param context
     */
    public static void init(Context context){
        try {
            vpnProfileClz = Class.forName("com.android.internal.net.VpnProfile");
            keyStoreClz = Class.forName("android.security.KeyStore");

            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            Field fieldIConManager = null;

            fieldIConManager = cm.getClass().getDeclaredField("mService");
            fieldIConManager.setAccessible(true);
            iConManagerObj = fieldIConManager.get(cm);
            iConManagerClz = Class.forName(iConManagerObj.getClass().getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * @param name     vpn连接名，自定义
     * @param server   服务器地址
     * @param username 用户名
     * @param password 用户密码
     * @return 返回一个com.android.internal.net.VpnProfile的实例
     */
    public static Object createVpnProfile(String name, String server, String username, String password,String type) {
        Object vpnProfileObj = null;
        try {
            //生成vpn的key
            long millis = System.currentTimeMillis();
            String vpnKey = Long.toHexString(millis);
            //获得构造函数
            Constructor constructor = vpnProfileClz.getConstructor(String.class);
            vpnProfileObj = constructor.newInstance(vpnKey);
            //设置参数
            setParams(vpnProfileObj,name,server,username,password,type);
            //插入vpn数据
            insertVpn(vpnProfileObj,vpnKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vpnProfileObj;
    }




    /**
     * @param name     vpn连接名，自定义
     * @param server   服务器地址
     * @param username 用户名
     * @param password 用户密码
     * @return 返回一个com.android.internal.net.VpnProfile的实例
     */
    public static Object setParams(Object vpnProfileObj,String name, String server, String username, String password ,String type) {
        try {
            Field field_username = vpnProfileClz.getDeclaredField("username");
            Field field_password = vpnProfileClz.getDeclaredField("password");
            Field field_server = vpnProfileClz.getDeclaredField("server");
            Field field_name = vpnProfileClz.getDeclaredField("name");
            Field field_saveLogin = vpnProfileClz.getDeclaredField("saveLogin");
            Field field_type = vpnProfileClz.getDeclaredField("type");
            Field field_key= vpnProfileClz.getDeclaredField("key");
            Field field_mppe= vpnProfileClz.getDeclaredField("mppe");
            //设置参数
            field_name.set(vpnProfileObj, name);
            field_server.set(vpnProfileObj, server);
            field_username.set(vpnProfileObj, username);
            field_password.set(vpnProfileObj, password);

            field_saveLogin.set(vpnProfileObj, true);
            if ("0".equals(type)){

                field_type.set(vpnProfileObj, 1);
            }else {
                field_type.set(vpnProfileObj, 0);
            }

            Log.w(TAG,"获取到1 field_username.get(vpnProfileObj)= "+ field_username.get(vpnProfileObj));
            Log.w(TAG,"获取到1 field_password.get(vpnProfileObj)= "+ field_password.get(vpnProfileObj));
            Log.w(TAG,"获取到1 field_server.get(vpnProfileObj)= "+ field_server.get(vpnProfileObj));
            Log.w(TAG,"获取到1 field_name.get(vpnProfileObj)= "+ field_name.get(vpnProfileObj));
            Log.w(TAG,"获取到1 field_key.get(vpnProfileObj)= "+ field_key.get(vpnProfileObj));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vpnProfileObj;
    }

    /**
     * 连接vpn
     * @param context
     * @param profile com.android.internal.net.VpnProfile的实例
     * @return true:连接成功，false:连接失败
     */
    public static boolean connect(Context context, Object profile) {


        try {
            Field field_username  = vpnProfileClz.getDeclaredField("username");
            Field field_password = vpnProfileClz.getDeclaredField("password");
            Field field_server = vpnProfileClz.getDeclaredField("server");
            Field field_name = vpnProfileClz.getDeclaredField("name");
            Log.w(TAG,"获取到3 field_username.get(vpnProfileObj)= "+ field_username.get(profile));
            Log.w(TAG,"获取到3 field_password.get(vpnProfileObj)= "+ field_password.get(profile));
            Log.w(TAG,"获取到3 field_server.get(vpnProfileObj)= "+ field_server.get(profile));
            Log.w(TAG,"获取到3 field_name.get(vpnProfileObj)= "+ field_name.get(profile));
        } catch (Exception e) {
            e.printStackTrace();
        }


        boolean isConnected = true;
        try {
            Method metStartLegacyVpn = iConManagerClz.getDeclaredMethod("startLegacyVpn", vpnProfileClz);
            metStartLegacyVpn.setAccessible(true);
            //解锁KeyStore
            unlock(context);
            //开启vpn连接
            metStartLegacyVpn.invoke(iConManagerObj, profile);
        } catch (Exception e) {
            isConnected = false;
          //  e.printStackTrace();
        }
        return isConnected;
    }

    /**
     * 断开vpn连接
     * @param context
     * @return true:已断开，false:断开失败
     */
    public static boolean disconnect(Context context) {
        boolean disconnected = true;
        try {
            Method metPrepare = iConManagerClz.getDeclaredMethod("prepareVpn", String.class, String.class);
            //断开连接
            metPrepare.invoke(iConManagerObj, "[Legacy VPN]", "[Legacy VPN]");
        } catch (Exception e) {
            disconnected = false;
            e.printStackTrace();
        }
        return disconnected;
    }

    /**
     * @return 返回一个已存在的vpn实例
     */
    public static Object getVpnProfile() {
        try {
            Object keyStoreObj = getKeyStoreInstance();

            Method keyStore_saw = keyStoreClz.getMethod("list",String.class);
            keyStore_saw.setAccessible(true);
            //查找数据库
            String[] keys = (String[]) keyStore_saw.invoke(keyStoreObj,"VPN_");
            //如果之前没有创建过vpn，则返回null
            if(keys == null || keys.length == 0){
                return null;
            }

            for(String s : keys){
                Log.w(TAG,"获取到keyStore= "+s);
                Log.i("key:",s);

                Method vpnProfile_decode = vpnProfileClz.getDeclaredMethod("decode", String.class, byte[].class);
                vpnProfile_decode.setAccessible(true);

                Method keyStore_get = keyStoreClz.getDeclaredMethod("get", String.class);
                keyStore_get.setAccessible(true);
                //获得第一个vpn
                Object byteArrayValue = keyStore_get.invoke(keyStoreObj,"VPN_"+s);
                //反序列化返回VpnProfile实例
                Object vpnProfileObj = vpnProfile_decode.invoke(null, s, byteArrayValue);



                Field field_username  = vpnProfileClz.getDeclaredField("username");
                Field field_password = vpnProfileClz.getDeclaredField("password");
                Field field_server = vpnProfileClz.getDeclaredField("server");
                Field field_name = vpnProfileClz.getDeclaredField("name");
                Field field_type = vpnProfileClz.getDeclaredField("type");
                Field key_type = vpnProfileClz.getDeclaredField("key");

                Log.w(TAG,"从keyStoreClz获取到 field_username.get(vpnProfileObj)= "+ field_username.get(vpnProfileObj));
                Log.w(TAG,"从keyStoreClz获取到 field_password.get(vpnProfileObj)= "+ field_password.get(vpnProfileObj));
                Log.w(TAG,"从keyStoreClz获取到 field_server.get(vpnProfileObj)= "+ field_server.get(vpnProfileObj));
                Log.w(TAG,"从keyStoreClz获取到 field_name.get(vpnProfileObj)= "+ field_name.get(vpnProfileObj));
                Log.w(TAG,"从keyStoreClz获取到 field_type.get(vpnProfileObj)= "+ field_type.get(vpnProfileObj));
                Log.w(TAG,"从keyStoreClz获取到 key_type.get(vpnProfileObj)= "+ key_type.get(vpnProfileObj));



            }

            Method vpnProfile_decode = vpnProfileClz.getDeclaredMethod("decode", String.class, byte[].class);
            vpnProfile_decode.setAccessible(true);

            Method keyStore_get = keyStoreClz.getDeclaredMethod("get", String.class);
            keyStore_get.setAccessible(true);
            //获得第一个vpn
            Object byteArrayValue = keyStore_get.invoke(keyStoreObj,"VPN_"+keys[0]);
            //反序列化返回VpnProfile实例
            Object vpnProfileObj = vpnProfile_decode.invoke(null, keys[0], byteArrayValue);

            Field field_username = vpnProfileClz.getDeclaredField("username");
            Field field_password = vpnProfileClz.getDeclaredField("password");
            Field field_server = vpnProfileClz.getDeclaredField("server");
            Field field_name = vpnProfileClz.getDeclaredField("name");

           /* Log.w(TAG,"获取到2 field_username.get(vpnProfileObj)= "+ field_username.get(vpnProfileObj));
            Log.w(TAG,"获取到2 field_password.get(vpnProfileObj)= "+ field_password.get(vpnProfileObj));
            Log.w(TAG,"获取到2 field_server.get(vpnProfileObj)= "+ field_server.get(vpnProfileObj));
            Log.w(TAG,"获取到2 field_name.get(vpnProfileObj)= "+ field_name.get(vpnProfileObj));*/

            return vpnProfileObj;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private static void insertVpn(Object profieObj,String key)throws Exception{

        try {
            Field field_username  = vpnProfileClz.getDeclaredField("username");
            Field field_password = vpnProfileClz.getDeclaredField("password");
            Field field_server = vpnProfileClz.getDeclaredField("server");
            Field field_name = vpnProfileClz.getDeclaredField("name");
            Field field_type = vpnProfileClz.getDeclaredField("type");
            Field key_type = vpnProfileClz.getDeclaredField("key");
            Log.w(TAG,"获取到4 field_username.get(vpnProfileObj)= "+ field_username.get(profieObj));
            Log.w(TAG,"获取到4 field_password.get(vpnProfileObj)= "+ field_password.get(profieObj));
            Log.w(TAG,"获取到4 field_server.get(vpnProfileObj)= "+ field_server.get(profieObj));
            Log.w(TAG,"获取到4 field_name.get(vpnProfileObj)= "+ field_name.get(profieObj));
            Log.w(TAG,"获取到4 field_type.get(vpnProfileObj)= "+ field_type.get(profieObj));
            Log.w(TAG,"获取到4 key_type.get(vpnProfileObj)= "+ key_type.get(profieObj));
        } catch (Exception e) {
            e.printStackTrace();
        }



        Method keyStore_put = keyStoreClz.getDeclaredMethod("put", String.class, byte[].class, int.class, int.class);
        Object keyStoreObj = getKeyStoreInstance();
        Class vpnProfileClz = Class.forName("com.android.internal.net.VpnProfile");
        Method vpnProfile_encode = vpnProfileClz.getDeclaredMethod("encode");
        byte[] bytes = (byte[]) vpnProfile_encode.invoke(profieObj);
        //  keyStore_put.invoke(keyStoreObj,"VPN_"+key,bytes,-1,1);
        keyStore_put.invoke(keyStoreObj,"VPN_"+key,bytes,-1,1);
        //  MDM.getVpnProfile(key);
    }

    private static Object getKeyStoreInstance() throws Exception {
        Method keyStore_getInstance = keyStoreClz.getMethod("getInstance");
        keyStore_getInstance.setAccessible(true);
        Object keyStoreObj = keyStore_getInstance.invoke(null);
        return keyStoreObj;
    }
    private static void unlock(Context mContext) throws Exception {
        credentialsClz = Class.forName("android.security.Credentials");

        Method credentials_getInstance = credentialsClz.getDeclaredMethod("getInstance");
        Object credentialsObj = credentials_getInstance.invoke(null);

        Method credentials_unlock = credentialsClz.getDeclaredMethod("unlock",Context.class);
        credentials_unlock.invoke(credentialsObj,mContext);
    }



    public static  /*boolean*/  void delete(String key) {
        try {
            Method declaredMethod = keyStoreClz.getDeclaredMethod("delete", String.class,int.class);
            Object keyStoreObj = getKeyStoreInstance();
            //  Boolean invoke = (Boolean) declaredMethod.invoke(keyStoreClz, key);
            declaredMethod.invoke (keyStoreObj,"VPN_"+key,-1);
        } catch (Exception e) {
            e.printStackTrace();
        }
         /*return delete(key, UID_SELF);*/
    }


    public  static  void  delete() {

        try {
            Object keyStoreObj = getKeyStoreInstance();

            Method keyStore_saw = keyStoreClz.getMethod("list", String.class);
            keyStore_saw.setAccessible(true);
            //查找数据库
            String[] keys = (String[]) keyStore_saw.invoke(keyStoreObj, "VPN_");
            //如果之前没有创建过vpn，则返回null
            if (keys == null || keys.length == 0) {
                return ;
            }
            Log.w(TAG, "从keyStoreClz获取到 删除vpnProfileObj ");
            for (String s : keys) {
                Log.w(TAG, "获取到keyStore= " + s);
                Log.i("key:", s);

                Method vpnProfile_decode = vpnProfileClz.getDeclaredMethod("decode", String.class, byte[].class);
                vpnProfile_decode.setAccessible(true);

                Method keyStore_get = keyStoreClz.getDeclaredMethod("get", String.class);
                keyStore_get.setAccessible(true);
                //获得第一个vpn
                Object byteArrayValue = keyStore_get.invoke(keyStoreObj, "VPN_" + s);
                //反序列化返回VpnProfile实例
                Object vpnProfileObj = vpnProfile_decode.invoke(null, s, byteArrayValue);

                Field field_username = vpnProfileClz.getDeclaredField("username");
                Field field_password = vpnProfileClz.getDeclaredField("password");
                Field field_server = vpnProfileClz.getDeclaredField("server");
                Field field_name = vpnProfileClz.getDeclaredField("name");
                Field field_type = vpnProfileClz.getDeclaredField("type");
                Field key_type = vpnProfileClz.getDeclaredField("key");

                Log.w(TAG, "从keyStoreClz获取到 field_username.get(vpnProfileObj)= " + field_username.get(vpnProfileObj));
                Log.w(TAG, "从keyStoreClz获取到 field_password.get(vpnProfileObj)= " + field_password.get(vpnProfileObj));
                Log.w(TAG, "从keyStoreClz获取到 field_server.get(vpnProfileObj)= " + field_server.get(vpnProfileObj));
                Log.w(TAG, "从keyStoreClz获取到 field_name.get(vpnProfileObj)= " + field_name.get(vpnProfileObj));
                Log.w(TAG, "从keyStoreClz获取到 field_type.get(vpnProfileObj)= " + field_type.get(vpnProfileObj));
                Log.w(TAG, "从keyStoreClz获取到 key_type.get(vpnProfileObj)= " + key_type.get(vpnProfileObj));
                delete(s);


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
