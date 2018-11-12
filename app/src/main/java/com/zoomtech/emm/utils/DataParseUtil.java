package com.zoomtech.emm.utils;

import android.content.ContentValues;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.zoomtech.emm.definition.Common;
import com.zoomtech.emm.features.manager.PreferencesManager;
import com.zoomtech.emm.features.policy.phoneCall.CallRecorderPolicyInfo;
import com.zoomtech.emm.features.policy.sms.SmsPolicyInfo;
import com.zoomtech.emm.features.presenter.MDM;
import com.zoomtech.emm.features.presenter.TheTang;
import com.zoomtech.emm.model.AppBlackWhiteData;
import com.zoomtech.emm.model.AppFenceData;
import com.zoomtech.emm.model.DeleteAppData;
import com.zoomtech.emm.model.DownLoadEntity;
import com.zoomtech.emm.model.ExceptionLogData;
import com.zoomtech.emm.model.GeographicalFenceData;
import com.zoomtech.emm.model.LostComplianceData;
import com.zoomtech.emm.model.MachineCardInfo;
import com.zoomtech.emm.model.PolicyData;
import com.zoomtech.emm.model.SafetyLimitData;
import com.zoomtech.emm.model.SecurityChromeData;
import com.zoomtech.emm.model.SensitiveStrategyInfo;
import com.zoomtech.emm.model.SettingAboutData;
import com.zoomtech.emm.model.SystemComplianceData;
import com.zoomtech.emm.model.TelephoyWhiteUser;
import com.zoomtech.emm.model.TimeData;
import com.zoomtech.emm.model.Token;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.RequestBody;


/**
 * 后台Push的json数据解析类
 */

public class DataParseUtil {
    private static final String TAG = "DataParseUtil";

    /**********************************************
     * JSON Parse
     ****************************************/

    /**
     * 基本解析
     *
     * @param target
     * @param extra
     * @return
     */
    public static String jSon(String target, String extra) {
        String code = null;
        try {
            JSONObject jsonObject = new JSONObject(extra);
            if (jsonObject != null) {
                code = jsonObject.getString(target);
            }
        } catch (JSONException e) {
            Log.d(TAG, "BaseJSONException");
            e.printStackTrace();
        }
        return code;
    }

    /**
     * 基本解析 opt 没有就默认返回""
     *
     * @param target
     * @param extra
     * @return
     */
    public static String jSonOpt(String target, String extra) {
        String code = null;
        try {
            JSONObject jsonObject = new JSONObject(extra);
            if (jsonObject != null) {
                code = jsonObject.optString(target);
            }
        } catch (JSONException e) {
            Log.d(TAG, "BaseJSONException");
            e.printStackTrace();
        }
        return code;
    }

    /**
     * 解析List<String>
     *
     * @param list
     * @param jsonName
     * @return
     */
    private synchronized static String parseList(List<String> list, String jsonName) {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = null;
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                try {
                    jsonObject = new JSONObject();
                    jsonObject.put(jsonName + String.valueOf(i), list.get(i));
                    jsonArray.put(jsonObject);
                } catch (JSONException e) {
                    Log.d(TAG, "This is list feedback JSONException!");
                    e.printStackTrace();
                }
            }
        }
        if (jsonArray != null) {
            return jsonArray.toString();
        }
        return null;
    }

    /**
     * 解析List<String> string 不是同类型
     *
     * @param list
     * @param data
     * @return
     */
    public synchronized static String parseList(List<String> list, String[] data) {
        JSONObject jsonObject = new JSONObject();
        if (list != null && list.size() > 0) {
            for (int i = 0; i < (list.size() >= data.length ? data.length : list.size()); i++) {
                try {
                    jsonObject.put(data[i], list.get(i));
                } catch (JSONException e) {
                    Log.d(TAG, "This is list JSONException!");
                    e.printStackTrace();
                }
            }
        }
        return jsonObject.toString();
    }

    /**
     * 命令码解析
     *
     * @param extra
     * @return
     */
    public synchronized static String jSonCode(String extra) {
        return jSon("code", extra);
    }

    /**
     * Enable命令
     *
     * @param extra
     * @return
     */
    public static boolean jSonEnable(String extra) {
        return Boolean.parseBoolean(jSon("enable", extra));
    }

    /**
     * String 命令解析
     *
     * @param key
     * @param extra
     * @return
     */
    public synchronized static String jSonString(String key, String extra) {
        return jSon(key, extra);
    }

    /**
     * 白名单解析
     *
     * @param extra
     * @return
     */
    public static List<TelephoyWhiteUser> jSonWhiteList(String extra) {
        Log.d(TAG, "白名单解析=" + extra);
        List<TelephoyWhiteUser> userlist = new ArrayList<TelephoyWhiteUser>();
        try {
            JSONObject jsonObject = new JSONObject(extra);
            JSONArray whiteArray = jsonObject.getJSONArray("whitelist");
            if (whiteArray != null && whiteArray.length() > 0) {
                for (int i = 0; i < whiteArray.length(); i++) {
                    TelephoyWhiteUser mTelephoyWhiteUser = new TelephoyWhiteUser();
                    JSONObject telephoywhiteuser = whiteArray.getJSONObject(i);

                    mTelephoyWhiteUser.setUserName(telephoywhiteuser.getString("username"));
                    mTelephoyWhiteUser.setUserId(telephoywhiteuser.getString("userId"));
                    //mTelephoyWhiteUser.setUserAddress(telephoywhiteuser.getString("userAddress"));
                    mTelephoyWhiteUser.setTelephonyNumber(telephoywhiteuser.getString("telephonyNumber"));
                    userlist.add(mTelephoyWhiteUser);
                }
            }
        } catch (JSONException e) {
            Log.d(TAG, "WhiteUserJSONException");
            e.printStackTrace();
        }
        return userlist;
    }

    /**
     * 安装应用解析
     *
     * @param code
     * @param extra
     * @return
     */
    public /*synchronized*/ static List<DownLoadEntity> jSonInstallApplicationList(String code, String extra) {
        List<DownLoadEntity> applist = new ArrayList<DownLoadEntity>();
        // APPInfo appInfo;
        try {
            DownLoadEntity entity = new DownLoadEntity();
            entity.code = code;
            JSONObject jsonObject = new JSONObject(extra);
            entity.app_id = jsonObject.getString("id");
            entity.sendId = jsonObject.getString("sendId");
            entity.internet = jsonObject.getString("is_internet");
            entity.uninstall = jsonObject.getString("is_uninstall");
            entity.packageName = jsonObject.getString("packageName");
            entity.version = jsonObject.getString("version");
            entity.type = "0";
            applist.add(entity);
        } catch (JSONException e) {
            Log.d(TAG, "AppListJSONException");
            e.printStackTrace();
        }
        return applist;
    }

    /**
     * 设备更新解析
     *
     * @param code
     * @param extra
     * @return
     */
    public synchronized static List<DownLoadEntity> jSonDeviceUpdate(String code, String extra) {
        List<DownLoadEntity> applist = new ArrayList<DownLoadEntity>();
        // APPInfo appInfo;
        try {
            DownLoadEntity entity = new DownLoadEntity();
            entity.code = code;
            JSONObject jsonObject = new JSONObject(extra);
            entity.app_id = jsonObject.getString("id");
            entity.sendId = jsonObject.getString("sendId");
            entity.internet = "1";
            entity.uninstall = "0";
            entity.packageName = Common.packageName;
            entity.version = jsonObject.getString("version");
            entity.type = "0";
            applist.add(entity);
        } catch (JSONException e) {
            Log.d(TAG, "DeviceUpdateListJSONException");
            e.printStackTrace();
        }
        return applist;
    }

    /**
     * 卸载应用解析
     *
     * @param code
     * @param extra
     * @return
     */
    public synchronized static List<DownLoadEntity> jSonUninstallApplicationList(String code, String extra) {
        List<DownLoadEntity> applist = new ArrayList<DownLoadEntity>();
        // APPInfo appInfo;
        try {
            DownLoadEntity entity = new DownLoadEntity();
            entity.code = code;
            JSONObject jsonObject = new JSONObject(extra);
            String id = jsonObject.getString("id");
            entity.app_id = id;
            entity.sendId = jsonObject.getString("sendId");
            applist.add(entity);
        } catch (JSONException e) {
            Log.d(TAG, "AppListJSONException");
            e.printStackTrace();
        }
        return applist;
    }

    /**
     * 文件解析
     *
     * @param extra
     * @return
     */
    public synchronized static List<DownLoadEntity> jSonFileNameList(String code, String extra) {
        Log.e(TAG, "文件解析=" + extra);
        List<DownLoadEntity> applist = new ArrayList<DownLoadEntity>();
        // APPInfo appInfo;
        try {
            DownLoadEntity entity = new DownLoadEntity();
            JSONObject jsonObject = new JSONObject(extra);
            entity.app_id = jsonObject.getString("id");
            entity.sendId = jsonObject.getString("sendId");
            entity.type = "1";
            entity.code = code;
            applist.add(entity);
        } catch (JSONException e) {
            Log.d(TAG, "FileListJSONException");
            e.printStackTrace();
        }
        return applist;
    }

    /**
     * wifi 配置文件转为WifiConfiguration
     *
     * @param extra
     * @return
     */
    public static WifiConfiguration jSonWifiConfiguration(String extra) {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        return wifiConfiguration;
    }

    /**
     * 将数据库中查询的白名单转为JSON
     *
     * @param listUser
     * @return
     */
    public synchronized static String parseWhiteListToString(List<TelephoyWhiteUser> listUser) {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = null;
        if (listUser != null && listUser.size() > 0) {
            for (int i = 0; i < listUser.size(); i++) {
                try {
                    jsonObject = new JSONObject();
                    jsonObject.put("userName", listUser.get(i).getUserName());
                    jsonObject.put("telephonyNumber", listUser.get(i).getTelephonyNumber());
                    jsonArray.put(jsonObject);
                } catch (JSONException e) {
                    Log.d(TAG, "White list feedback JSONException!");
                    e.printStackTrace();
                }
            }
        }
        if (jsonObject != null) {
            return jsonObject.toString();
        }
        return null;
    }

    /*Contact Info 解析
      buf.append("id=" + id);
        如果通过id查询数据库：Uri.parse("content://com.android.contacts/contacts/" + id + "/data");
        如果有，则添加
            buf.append(",name=" + data);
            buf.append(",phone=" + data);
            buf.append(",email=" + data);
            buf.append(",address=" + data);
            buf.append(",organization=" + data);
        结束
        buf.append("\n");
     */
    public synchronized static String parseContactInfo(String allContactInfo) {
        String[] singleContactInfo = allContactInfo.split("\n");
        JSONArray jsonArray = new JSONArray();
        for (String contactInfo : singleContactInfo) {
            String[] infos = contactInfo.split(",");
            JSONObject jsonObject = new JSONObject();
            if (infos != null && infos.length > 1) {
                for (int i = 1; i < infos.length; i++) {
                    String[] keys = infos[i].split("==");
                    try {
                        jsonObject.put(keys[0], keys[1]);
                    } catch (JSONException e) {
                        Log.d(TAG, "ContactInfo JSONException!");
                        e.printStackTrace();
                    }
                }
            }
            jsonArray.put(jsonObject);
        }
        if (jsonArray != null) {
            return jsonArray.toString();
        }
        return null;
    }

    /**
     * APN ContentValues 解析
     * ContentValues:
     * "_id"、"name"、"apn", "myapn"、"type"、"numeric"、"mcc"、"mnc"、"proxy"、
     * "port"、"mmsproxy"、"mmsport"、"user"、"server"、"password"、"mmsc"
     *
     * @param contentValues
     * @return 解析APN
     */
    public synchronized static String parseAPNContentValues(ContentValues contentValues) {
        JSONObject jsonObject = new JSONObject();
        try {
            if (contentValues != null) {
                jsonObject.put("_id", contentValues.get("_id"));
                jsonObject.put("name", contentValues.get("name"));
                jsonObject.put("apn", contentValues.get("myapn"));
                jsonObject.put("type", contentValues.get("type"));
                jsonObject.put("numeric", contentValues.get("numeric"));
                jsonObject.put("mcc", contentValues.get("mcc"));
                jsonObject.put("mnc", contentValues.get("mnc"));
                jsonObject.put("proxy", contentValues.get("proxy"));
                jsonObject.put("port", contentValues.get("port"));
                jsonObject.put("mmsproxy", contentValues.get("mmsproxy"));
                jsonObject.put("mmsport", contentValues.get("mmsport"));
                jsonObject.put("user", contentValues.get("user"));
                jsonObject.put("server", contentValues.get("server"));
                jsonObject.put("password", contentValues.get("password"));
                jsonObject.put("mmsc", contentValues.get("mmsc"));
            }
        } catch (JSONException e) {
            Log.d(TAG, "Parse APN ContentValues JSONException!");
            e.printStackTrace();
        }
        if (jsonObject != null) {
            return jsonObject.toString();
        }
        return null;
    }

    /**
     * 解析APN ContentValues
     *
     * @param contentValues
     * @return
     */
    public synchronized static ContentValues jSonAPNContentValues(String contentValues) {
        ContentValues contentValues1 = new ContentValues();
        try {
            JSONObject jsonObject = new JSONObject(contentValues);
            if (jsonObject != null) {
                contentValues1.put("name", jsonObject.getString("name"));
                contentValues1.put("apn", jsonObject.getString("myapn"));
                contentValues1.put("type", jsonObject.getString("type"));
                contentValues1.put("numeric", jsonObject.getString("numeric"));
                contentValues1.put("mcc", jsonObject.getString("mcc"));
                contentValues1.put("mnc", jsonObject.getString("mnc"));
                contentValues1.put("proxy", jsonObject.getString("proxy"));
                contentValues1.put("port", jsonObject.getString("port"));
                contentValues1.put("mmsproxy", jsonObject.getString("mmsproxy"));
                contentValues1.put("mmsport", jsonObject.getString("mmsport"));
                contentValues1.put("user", jsonObject.getString("user"));
                contentValues1.put("server", jsonObject.getString("server"));
                contentValues1.put("password", jsonObject.getString("password"));
                contentValues1.put("mmsc", jsonObject.getString("mmsc"));
            }
        } catch (JSONException e) {
            Log.d(TAG, "JSON APN ContentValues JSONException!");
            e.printStackTrace();
        }
        return contentValues1;
    }

    /**
     * 反馈给服务器的定位数据转JSON
     *
     * @param location
     * @return
     */
    public synchronized static String jsonLocation(String location) {
        JSONObject jsonObject = new JSONObject();
        if (location != null) {
            String[] locations = location.split(",");
            try {
                jsonObject.put("longitude", locations[0]);
                jsonObject.put("latitude", locations[1]);
            } catch (JSONException e) {
                Log.d(TAG, "Location JSONException");
                e.printStackTrace();
            }
        }
        if (jsonObject != null) {
            return jsonObject.toString();
        }
        return null;
    }

    /*
    WifiConfiguration参数
    "ID"、"SSID"、"PROVIDER-NAME"、"BSSID"、"FQDN"、"PRIO"、"KeyMgmt"、"Protocols"、"AuthAlgorithms"、
    "PairwiseCiphers"、"GroupCiphers"、"PSK"、"Enterprise config"、"IP config"、"IP assignment"、"Proxy settings"、
    "roamingFailureBlackListTimeMilli"、"triggeredLow"、"triggeredBad"、"tridderedNotHigh"、"ticksLow"、
    "ticksBad"、"ticksNotHigh"、"triggeredJoin"、"autoJoinBailedDueToLowRssi"、"autoJoinUseAggressiveJoinAttemptThreshold"
    */

    /**
     * 登录数据转为JSON
     *
     * @param username
     * @param passWord
     * @param deviceInfo
     * @return
     */
    public synchronized static RequestBody loginToJson(Context context, String username, String passWord, List<String> deviceInfo) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("loginName", username);
            jsonObject.put("code", passWord);
            jsonObject.put("app_version", AppUtils.getAppVersionName(context, Common.packageName));
            jsonObject.put("packageName", Common.packageName);

            //通过包名判断系统
            //if ("com.zoomtech.emm".equals(Common.packageName)) {
//                jsonObject.put( "adaptSystem", "2" );
            //} else {
            jsonObject.put("adaptSystem", "1");
            //}

            if (deviceInfo != null && deviceInfo.size() > 0) {
                for (int i = 0; i < (deviceInfo.size() >= Common.deviceInfo.length ? Common.deviceInfo.length : deviceInfo.size()); i++) {
                    try {
                        jsonObject.put(Common.deviceInfo[i], deviceInfo.get(i));
                    } catch (JSONException e) {
                        Log.d(TAG, "This is list JSONException!");
                        e.printStackTrace();
                    }
                }
            }
            jsonObject.put("telephonyNumber", PhoneUtils.getTelePhonyNumber(TheTang.getSingleInstance().getContext()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse(
                "application/json;charset=UTF-8"), jsonObject.toString());
        return body;
    }

    /**
     * 反馈给服务器的数据转为JSON
     *
     * @param feedback_code
     * @param result
     * @return
     */
    public synchronized static String feedbackToJson(String sendId, String feedback_code, String result) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("alias", PreferencesManager.getSingleInstance().getData("alias"));
            jsonObject.put("feedback_code", feedback_code);
            jsonObject.put("result", result);
            jsonObject.put("sendId", sendId);
        } catch (Exception e) {
            LogUtil.writeToFile(TAG, "Feedback to Json " + e.getCause().toString());
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    /**
     * 反馈给服务器的数据转为JSON
     *
     * @param feedback_code
     * @param file_id
     * @param result
     * @return
     */
    public synchronized static String feedbackToJson(String sendId, String feedback_code, String file_id, String result) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("alias", PreferencesManager.getSingleInstance().getData("alias"));
            jsonObject.put("feedback_code", feedback_code);
            jsonObject.put("id", file_id);
            jsonObject.put("result", result);
            jsonObject.put("sendId", sendId);
        } catch (Exception e) {
            LogUtil.writeToFile(TAG, "Feedback to Json " + e.getCause().toString());
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    /**
     * 反馈给服务器的数据转为JSON
     *
     * @param feedback_code
     * @param file_id
     * @param result
     * @return
     */
    public synchronized static RequestBody feedbackToJsonConpliance(String sendId, String feedback_code, String file_id, String send_id, String result) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("alias", PreferencesManager.getSingleInstance().getData("alias"));
            jsonObject.put("feedback_code", feedback_code);
            jsonObject.put("id", file_id);
            jsonObject.put("sendId", send_id);
        } catch (Exception e) {
            LogUtil.writeToFile(TAG, "Feedback to Json " + e.getCause().toString());
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse(
                "application/json;charset=UTF-8"), jsonObject.toString());
        return body;
    }

    /**
     * 登录反馈数据解析
     *
     * @param jsonObject
     * @return
     */
    public synchronized static Token loginBackParse(JSONObject jsonObject) {
        final Token token = new Token();
        try {
            String message = jsonObject.getString("message");
            LogUtil.writeToFile(TAG, message);
            final JSONObject object1 = new JSONObject(message);
            if (object1 != null) {
                token.setAccess_token(object1.getString(Common.token));
                token.setUser_alias(object1.getString(Common.alias));
                token.setKeepAliveHost(object1.getString(Common.keepAliveHost));
                token.setKeepAlivePort(object1.getString(Common.keepAlivePort));
                TheTang.getSingleInstance().getThreadPoolObject().submit(new Runnable() {
                    @Override
                    public void run() {
                        /**
                         * 添加白名单状态
                         */
                        String openWhiteList = null;
                        try {
                            openWhiteList = object1.getString("openWhiteList");
                            if (openWhiteList != null && "1".equals(openWhiteList)) {
                                MDM.getSingleInstance().startPhoneWhite();
                            } else {
                                MDM.getSingleInstance().stopPhoneWhite();
                            }
                            /*List<TelephoyWhiteUser> list = new ArrayList<>();
                            JSONArray jsonArray1 = object1.getJSONArray( "whiteList1" );
                            if (jsonArray1 != null && jsonArray1.length() > 0) {
                                for (int i = 0; i < jsonArray1.length(); i++) {
                                    TelephoyWhiteUser mTelephoyWhiteUser = new TelephoyWhiteUser();
                                    JSONObject telephoywhiteuser = jsonArray1.getJSONObject( i );
                                    mTelephoyWhiteUser.setUserName( telephoywhiteuser.getString( "username" ) );
                                    mTelephoyWhiteUser.setUserId( telephoywhiteuser.getString( "userId" ) );
                                    //mTelephoyWhiteUser.setUserAddress(telephoywhiteuser.getString("userAddress"));
                                    mTelephoyWhiteUser.setTelephonyNumber( telephoywhiteuser.getString( "telephonyNumber" )  );
                                    list.add( mTelephoyWhiteUser );
                                }
                            }

                            JSONArray jsonArray = object1.getJSONArray( "whiteList" );
                            if (jsonArray != null) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject object2 = jsonArray.getJSONObject( i );
                                    TelephoyWhiteUser whiteUser = new TelephoyWhiteUser();
                                    whiteUser.setUserName( object2.getString( "username" ) );
                                    whiteUser.setUserId( object2.getString( "userId" ) );
                                    //whiteUser.setUserAddress(object2.getString("userAddress"));
                                    whiteUser.setTelephonyNumber( object2.getString( "telephonyNumber" ) );
                                    list.add( whiteUser );
                                }
                            }*/

                            //存储设置相关数据
                            SettingAboutData settingAboutData = new SettingAboutData();
                            JSONObject jsonObject1 = object1.getJSONObject(Common.setting_clientManagement);
                            settingAboutData.messageForHelp = jsonObject1.getString(Common.setting_help);
                            settingAboutData.agreementLicense = jsonObject1.getString(Common.setting_agreement);
                            settingAboutData.supportContent = jsonObject1.getString(Common.setting_stand_by);
                            TheTang.getSingleInstance().storageSettingAboutData(settingAboutData);

                           /* DatabaseOperate databaseOperate = DatabaseOperate.getSingleInstance();
                            databaseOperate.addTelephonyWhiteList( list );
                            //insert telephony white data
                            for (TelephoyWhiteUser mTelephoyWhiteUser: list) {
                                MDM.insertContact( mTelephoyWhiteUser.getUserName(), mTelephoyWhiteUser.getTelephonyNumber() );
                            }
                            token.setWhiteList( list );*/
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return token;
    }

    /**
     * 限制策略
     *
     * @param extra
     * @return
     */
    public synchronized static PolicyData jsonPolicyData(String extra) {
        Log.e(TAG, "限制策略=" + extra);
        PolicyData policyData = new PolicyData();
        try {
            JSONObject policy = new JSONObject(extra);
            JSONArray policyArray = policy.getJSONArray(Common.middle_policy);
            if (policyArray != null) {
                for (int i = 0; i < policyArray.length(); i++) {
                    JSONObject policyOrder = policyArray.getJSONObject(i);
                    policyData.name = policyOrder.getString("name");
                    //policyData.id = policyOrder.getString( "id" );
                    policyData.allowUpdateTime = policyOrder.getString(Common.middle_allowUpdateTime);
                    policyData.allowSoundRecording = policyOrder.getString(Common.middle_allowSoundRecording);
                    policyData.allowMobileData = policyOrder.getString(Common.allowMobileData);
                    policyData.allowCamera = policyOrder.getString(Common.middle_allowCamera);
                    policyData.allowSdCard = policyOrder.getString(Common.middle_allowSdCard);
                    policyData.allowUsb = policyOrder.getString(Common.middle_allowUsb);
                    policyData.allowLocation = policyOrder.getString(Common.middle_allowLocation);

                    policyData.allowMobileHotspot = policyOrder.getString(Common.middle_allowMobileHotspot);
                    policyData.allowWifi = policyOrder.getString(Common.middle_allowWifi);
                    //policyData.allowRestoreFactorySettings = policyOrder.getString( Common.middle_allowRestoreFactorySettings );

                    policyData.allowMessage = policyOrder.getString(Common.middle_allowMessage);
                    policyData.allowBluetooth = policyOrder.getString(Common.middle_allowBluetooth);

                    policyData.allowScreenshot = policyOrder.getString(Common.middle_allowScreenshot);
                    policyData.allowDropdown = policyOrder.getString(Common.middle_allowDropdown);
                    policyData.allowReset = policyOrder.getString(Common.middle_allowReset);
                    policyData.allowNFC = policyOrder.getString(Common.middle_allowNFC);
                    policyData.allowModifySystemtime = policyOrder.getString(Common.middle_allowModifySystemtime);

                    policyData.allowTelephone = policyOrder.getString(Common.middle_telephone);
                    policyData.allowTelephoneWhiteList = policyOrder.getString(Common.middle_telephoneWhiteList);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtil.writeToFile(TAG, LogUtil.getExceptionInfo(e));
        }
        return policyData;
    }

    /**
     * 失联合规解析
     *
     * @param extra
     * @return
     */
    public synchronized static LostComplianceData jSonLostCompilance(String extra) {
        LostComplianceData lostComplianceData = new LostComplianceData();
        try {
            JSONObject lost = new JSONObject(extra);
            lostComplianceData.lost_compliance = "true";
            lostComplianceData.missingId = lost.getInt("id");
            lostComplianceData.lost_name = lost.getString("name");
            lostComplianceData.lost_time = lost.getString(Common.lost_time);
            String compliance_excute = lost.getString("missingHandle");
            if ("0".equals(compliance_excute)) {
                lostComplianceData.lost_password = lost.getString("lockPwd");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return lostComplianceData;
    }

    /**
     * 失联合规解析
     *
     * @param extra
     * @return
     */
    public synchronized static SystemComplianceData jSonSystemCompilance(String extra) {
        SystemComplianceData systemComplianceData = new SystemComplianceData();
        try {
            JSONObject system = new JSONObject(extra);
            systemComplianceData.systemCompliance = "true";
            systemComplianceData.systemComplianceId = system.getString("id");
            systemComplianceData.systemComplianceName = system.getString("name");
            //systemComplianceData.systemComplianceDelayHour = system.getString( Common.system_compliance_delay );
            String compliance_excute = system.getString("illegalHandle");
            if ("0".equals(compliance_excute)) {
                systemComplianceData.lockPwd = system.getString("lockPwd");
            }
            JSONArray complianceStrategy = system.getJSONArray("complianceStrategy");
            for (int i = 0; i < complianceStrategy.length(); i++) {
                /*String stratege = system.optString( Common.system_info[i] );

                if (TextUtils.isEmpty( stratege )) {
                    continue;
                }*/
                switch (Integer.parseInt(complianceStrategy.optString(i))) {
                    case 0:
                        systemComplianceData.systemSd = "true";
                        break;
                    case 1:
                        systemComplianceData.systemSim = "true";
                        break;
                    /*case 2:
                        systemComplianceData.systemSim = "true";
                        break;
                    case 3:
                        systemComplianceData.systemEncryption = "true";
                        break;
                    case 4:
                        systemComplianceData.systemRoot = "true";
                        break;
                    case 5:
                        systemComplianceData.systemStatistics = "true";
                        break;*/
                    default:
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return systemComplianceData;
    }

    /**
     * 解析应用合规数据
     *
     * @param extra
     * @return
     */
    public synchronized static AppBlackWhiteData jSonAppCompilance(String extra) {
        AppBlackWhiteData appBlackWhiteData = new AppBlackWhiteData();
        try {
            JSONObject appObject = new JSONObject(extra);
            appBlackWhiteData.id = appObject.getString("id");
            appBlackWhiteData.name = appObject.getString("name");
            appBlackWhiteData.type = appObject.getString(Common.appType);
            appBlackWhiteData.appList = new ArrayList<>();

            String compliance_excute = appObject.getString("illegalHandle");
            if ("0".equals(compliance_excute)) {
                appBlackWhiteData.lockPwd = appObject.getString("lockPwd");
            }
            JSONArray jsonArray = appObject.getJSONArray(Common.appList);
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    appBlackWhiteData.appList.add(jsonArray.get(i).toString());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return appBlackWhiteData;
    }

    /**
     * 解析地理围栏
     *
     * @param extra
     * @return
     */
    public synchronized static GeographicalFenceData jSonGeographicalFence(String extra) {
        GeographicalFenceData geographicalFenceData = new GeographicalFenceData();
        try {
            JSONObject fenceObject = new JSONObject(extra);
            geographicalFenceData.geographical_fence = "true";
            JSONArray geographicalArray = fenceObject.getJSONArray("policy");
            for (int i = 0; i < geographicalArray.length(); i++) {
                JSONObject strategeObject = geographicalArray.getJSONObject(i);
                geographicalFenceData.geographical_fence_name = strategeObject.getString(Common.geographical_fence_name);
                String[] coordinate = strategeObject.getString(Common.coordinate).split(",");
                geographicalFenceData.fence_longitude = coordinate[0];
                geographicalFenceData.fence_latitude = coordinate[1];
                geographicalFenceData.radius = strategeObject.getString(Common.radius);
                //geographicalFenceData.geo_id = strategeObject.getString( "id" );
                jsonConfigurationFence(geographicalFenceData, strategeObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            String str = e.toString();
            LogUtil.writeToFile(TAG, "json geographical fence " + e.toString());
            Log.w(TAG, LogUtil.getExceptionInfo(e));
        }
        return geographicalFenceData;
    }

    /**
     * 解析地理与时间围栏的配置
     *
     * @param geographicalFenceData
     * @param strategeObject
     */
    private synchronized static void jsonConfigurationFence(GeographicalFenceData geographicalFenceData, JSONObject strategeObject) {
        jsonDeviceConfiguration(geographicalFenceData, strategeObject);
        jsonSecurityChrome(geographicalFenceData, strategeObject);
        jsonCustomDesktop(geographicalFenceData, strategeObject);
        jsonDoubleDomain(geographicalFenceData, strategeObject);
    }

    /**
     * 解析设备配置
     *
     * @param geographicalFenceData
     * @param strategeObject
     */
    private synchronized static void jsonDeviceConfiguration(GeographicalFenceData geographicalFenceData, JSONObject strategeObject) {
        try {
            if ("null".equals(strategeObject.getString(Common.lockScreen)) || "2".equals(strategeObject.getString(Common.lockScreen))) {
                geographicalFenceData.allowDevice = "false";
                return;
            }
            geographicalFenceData.allowDevice = "true";
            geographicalFenceData.lockScreen = strategeObject.getString(Common.lockScreen);
            geographicalFenceData.lockPassword = strategeObject.getString("lockPwd");
            geographicalFenceData.allowMobileData = strategeObject.getString(Common.allowMobileData);
            geographicalFenceData.allowCloseWifi = strategeObject.getString(Common.allowCloseWifi);

            geographicalFenceData.allowOpenWifi = strategeObject.getString(Common.allowOpenWifi);
            geographicalFenceData.allowConfigureWifi = strategeObject.getString(Common.allowConfigureWifi);
            geographicalFenceData.configureWifi = strategeObject.getString(Common.configureWifi);
            geographicalFenceData.allowAutomaticJoin = strategeObject.getString(Common.allowAutomaticJoin);
            geographicalFenceData.hiddenNetwork = strategeObject.getString(Common.hiddenNetwork);
            geographicalFenceData.safeType = strategeObject.getString(Common.safeType);
            geographicalFenceData.wifiPassword = strategeObject.getString(Common.wifi_password);

            geographicalFenceData.allowCamera = strategeObject.getString(Common.allowCamera);
            geographicalFenceData.allowBluetooth = strategeObject.getString(Common.allowBluetooth);
            geographicalFenceData.allowContainSwitching = strategeObject.getString(Common.allowContainSwitching);

            geographicalFenceData.mobileHotspot = strategeObject.getString(Common.mobileHotspot);
            //geographicalFenceData.locationService = strategeObject.getString( Common.locationService );
            geographicalFenceData.matTransmission = strategeObject.getString(Common.matTransmission);
            geographicalFenceData.shortMessage = strategeObject.getString(Common.shortMessage);
            geographicalFenceData.soundRecording = strategeObject.getString(Common.soundRecording);

            geographicalFenceData.banScreenshot = strategeObject.getString(Common.banScreenshot);
            geographicalFenceData.allowDropdown = strategeObject.getString(Common.allowDropdown);
            geographicalFenceData.allowReset = strategeObject.getString(Common.allowReset);
            geographicalFenceData.allowNFC = strategeObject.getString(Common.allowNFC);
            geographicalFenceData.allowModifySystemtime = strategeObject.getString(Common.allowModifySystemtime);

            geographicalFenceData.telephone = strategeObject.getString(Common.geo_telephone);
            geographicalFenceData.telephoneWhiteList = strategeObject.getString(Common.geo_telephoneWhiteList);
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtil.writeToFile(TAG, "json device configuration " + e.getCause().toString());
            Log.w(TAG, LogUtil.getExceptionInfo(e));
        }
    }

    /**
     * 解析安全浏览器
     *
     * @param geographicalFenceData
     * @param strategeObject
     */
    private synchronized static void jsonSecurityChrome(GeographicalFenceData geographicalFenceData, JSONObject strategeObject) {
        try {
            if ("null".equals(strategeObject.getString(Common.webPageList)) || "2".equals(strategeObject.getString(Common.webPageList))) {
                Log.w(TAG, "strategeObject.getString( Common.webPageList )=" + strategeObject.getString(Common.webPageList));
                geographicalFenceData.allowChrome = null;
                return;
            } else {
                if ("1".equals(strategeObject.getString(Common.webPageList))) {
                    geographicalFenceData.allowChrome = "true";
                } else {
                    geographicalFenceData.allowChrome = "false";
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.w(TAG, LogUtil.getExceptionInfo(e));
        }
    }

    /**
     * 解析定制桌面配置
     *
     * @param geographicalFenceData
     * @param strategeObject
     */
    private synchronized static void jsonCustomDesktop(GeographicalFenceData geographicalFenceData, JSONObject strategeObject) {
        try {
            if ("null".equals(strategeObject.getString(Common.setToSecureDesktop)) || "2".equals(strategeObject.getString(Common.setToSecureDesktop))) {
                geographicalFenceData.allowDesktop = "false";
                return;
            }
            geographicalFenceData.allowDesktop = "true";
            geographicalFenceData.setToSecureDesktop = strategeObject.getString(Common.setToSecureDesktop);
            geographicalFenceData.displayContacts = strategeObject.getString(Common.displayContacts);
            geographicalFenceData.displayMessage = strategeObject.getString(Common.displayMessage);
            geographicalFenceData.displayCall = strategeObject.getString(Common.displayCall);
            geographicalFenceData.json_Apploication = strategeObject.getString(Common.applicationProgram);
            Log.w(TAG, "--json_Apploication--" + geographicalFenceData.json_Apploication);
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtil.writeToFile(TAG, "json custom desktop " + e.getCause().toString());
            Log.w(TAG, LogUtil.getExceptionInfo(e));
        }
    }

    /**
     * 解析双域
     *
     * @param geographicalFenceData
     * @param strategeObject
     */
    private synchronized static void jsonDoubleDomain(GeographicalFenceData geographicalFenceData, JSONObject strategeObject) {
        try {
            if ("null".equals(strategeObject.getString(Common.twoDomainControl)) || "2".equals(strategeObject.getString(Common.twoDomainControl))) {
                geographicalFenceData.allowDoubleDomain = "false";
                return;
            }
            geographicalFenceData.allowDoubleDomain = "true";
            geographicalFenceData.twoDomainControl = strategeObject.getString(Common.twoDomainControl);
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtil.writeToFile(TAG, "json double domain " + e.getCause().toString());
            Log.w(TAG, LogUtil.getExceptionInfo(e));
        }
    }

    /**
     * 应用黑白名单解析
     *
     * @param extra
     * @return
     */
    public synchronized static AppBlackWhiteData jsonBlackWhiteList(String extra) {
        AppBlackWhiteData appBlackWhiteData = new AppBlackWhiteData();
        try {
            JSONObject jsonObject = new JSONObject(extra);
            appBlackWhiteData.id = jsonObject.getString("id");
            appBlackWhiteData.type = jsonObject.getString("type");
            appBlackWhiteData.name = jsonObject.getString("name");
            appBlackWhiteData.appList = new ArrayList<>();
            JSONArray jsonArray = jsonObject.getJSONArray("list");
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    String packageName = jsonArray.getJSONObject(i).getString("appPackage");
                    appBlackWhiteData.appList.add(packageName);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            String str = e.toString();
            LogUtil.writeToFile(TAG, "json black white list " + str);
        }
        return appBlackWhiteData;
    }

    /**
     * 机卡绑定解析
     *
     * @param extra
     */
    public synchronized static MachineCardInfo jsonMachineCard(String extra) {
        MachineCardInfo machineCardInfo = new MachineCardInfo();
        try {
            JSONObject jsonObject = new JSONObject(extra);
            machineCardInfo.machineCard = "true";
            JSONArray jsonArray = jsonObject.getJSONArray("policy");
            //machineCardInfo.iccid = jsonArray.getJSONObject( 0 ).getString( Common.iccid_card );
            //machineCardInfo.imei = jsonArray.getJSONObject( 0 ).getString( Common.imei_phone );
            LogUtil.writeToFile(TAG, "iccid = " + machineCardInfo.iccid + "," + "imei = " + machineCardInfo.imei);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.w(TAG, LogUtil.getExceptionInfo(e));
        }
        return machineCardInfo;
    }

    /**
     * 异常Log 解析
     *
     * @param extra
     * @return
     */
    public static ExceptionLogData jsonExceptionLog(String extra) {
        ExceptionLogData exceptionLogData = new ExceptionLogData();
        exceptionLogData.logId = DataParseUtil.jSon("id", extra);
        exceptionLogData.isWifiUpload = DataParseUtil.jSon("isWifiUpload", extra);
        exceptionLogData.date = DataParseUtil.jSon("date", extra);
        return exceptionLogData;
    }

    /**
     * 安全浏览器解析
     *
     * @param extra
     * @return
     */
    public static SecurityChromeData jsonSecurityData(String extra) {
        SecurityChromeData securityChromeData = new SecurityChromeData();
        try {
            JSONObject jsonObject = new JSONObject(extra);
            JSONArray jsonArray = jsonObject.getJSONArray("policy");
            securityChromeData.sec_name = jsonArray.getJSONObject(0).getString("name");
            //securityChromeData.sec_id = jsonArray.getJSONObject( 0 ).getString( "id" );
            JSONArray list = jsonArray.getJSONObject(0).getJSONArray("explorerWhitelist");
            securityChromeData.sec_white_list = new HashMap<>();
            if (list != null) {
                for (int i = 0; i < list.length(); i++) {
                    try {
                        securityChromeData.sec_white_list.put(list.getJSONObject(i).getString("whitelistName"),
                                list.getJSONObject(i).getString("whitelistAddress"));
                    } catch (Exception e) {
                        LogUtil.writeToFile(TAG, "SecurityChromeData: " + e.getCause().toString());
                        Log.w(TAG, LogUtil.getExceptionInfo(e));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.w(TAG, LogUtil.getExceptionInfo(e));
        }
        return securityChromeData;
    }

    /**
     * 解析要删除的app的数据
     *
     * @param orderCode
     * @param extra
     * @return
     */
    public static DeleteAppData jsonDeleteAppData(String orderCode, String extra) {
        DeleteAppData deleteAppData = new DeleteAppData();
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(extra);
            deleteAppData.packageName = jsonObject.getString("packageName");
            deleteAppData.app_id = jsonObject.getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.w(TAG, LogUtil.getExceptionInfo(e));
        }
        return deleteAppData;
    }

    /**
     * 解析设置相关数据
     *
     * @param extra
     * @return
     */
    public static SettingAboutData jsonSettingAboutData(String extra) {
        SettingAboutData settingAboutData = new SettingAboutData();
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(extra);
            JSONObject jsonObject1 = jsonObject.getJSONObject(Common.setting_clientManagement);
            settingAboutData.messageForHelp = jsonObject1.getString(Common.setting_help);
            settingAboutData.agreementLicense = jsonObject1.getString(Common.setting_agreement);
            settingAboutData.supportContent = jsonObject1.getString(Common.setting_stand_by);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.w(TAG, LogUtil.getExceptionInfo(e));
        }
        return settingAboutData;
    }

    /**
     * 解析用Gson送数据
     */
    public static <T> T jsonToData(Class<T> clazz, String extra) {
        T t = null;
        try {
            t = new Gson().fromJson(extra, clazz);
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, LogUtil.getExceptionInfo(e));
        }
        return t;
    }

    /**
     * 头像解析
     *
     * @param extra
     * @return
     */
    public static List<DownLoadEntity> jSonPictureNameList(String code, String extra) {
        Log.e(TAG, "头像解析=" + extra);
        List<DownLoadEntity> applist = new ArrayList<DownLoadEntity>();
        // APPInfo appInfo;
        try {
            DownLoadEntity entity = new DownLoadEntity();
            JSONObject jsonObject = new JSONObject(extra);
            entity.app_id = "000";
            entity.sendId = jsonObject.getString("sendId");
            entity.type = "2";
            entity.code = code;
            applist.add(entity);
        } catch (JSONException e) {
            Log.d(TAG, "PictureListJSONException");
            e.printStackTrace();
        }
        return applist;
    }

    /**
     * 安全配置解析
     *
     * @param extra
     * @return
     */
    public static SafetyLimitData jsonSafetyLimitData(String extra) {
        SafetyLimitData safetyLimitData = new SafetyLimitData();
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(extra);
            JSONObject jsonObject1 = jsonObject.getJSONObject(Common.banSecurity);

            safetyLimitData.banCamera = "0".equals(jsonObject1.getString(Common.banCamera)) ? "1" : "0";
            safetyLimitData.banWifi = "0".equals(jsonObject1.getString(Common.banWifi)) ? "1" : "0";
            safetyLimitData.banMobileData = "0".equals(jsonObject1.getString(Common.banMobileData)) ? "1" : "0";
            safetyLimitData.banBluetooth = "0".equals(jsonObject1.getString(Common.banBluetooth)) ? "1" : "0";
            safetyLimitData.banLocation = "0".equals(jsonObject1.getString(Common.banLocation)) ? "1" : "0";
            safetyLimitData.banMtp = "0".equals(jsonObject1.getString(Common.banMtp)) ? "1" : "0";
            safetyLimitData.banSoundRecord = "0".equals(jsonObject1.getString(Common.banSoundRecord)) ? "1" : "0";
            //safetyLimitData.banTelephone = jsonObject1.getString( Common.banTelephone );
            safetyLimitData.banExitSecurityDomain = "0".equals(jsonObject1.getString(Common.banExitSecurityDomain)) ? "1" : "0";

            safetyLimitData.banScreenshot = "0".equals(jsonObject1.getString(Common.banScreenshot)) ? "1" : "0";
            safetyLimitData.allowDropdown = "0".equals(jsonObject1.getString(Common.allowDropdown)) ? "1" : "0";
            safetyLimitData.allowReset = "0".equals(jsonObject1.getString(Common.allowReset)) ? "1" : "0";
            safetyLimitData.allowNFC = "0".equals(jsonObject1.getString(Common.allowNFC)) ? "1" : "0";
            safetyLimitData.allowModifySystemtime = "0".equals(jsonObject1.getString(Common.allowModifySystemtime)) ? "1" : "0";

            safetyLimitData.banTelephone = "0".equals(jsonObject1.getString(Common.banTelephone)) ? "1" : "0";
            safetyLimitData.banTelephoneWhiteList = "0".equals(jsonObject1.getString(Common.banTelephoneWhiteList)) ? "1" : "0";
            safetyLimitData.banMobileHotspot = "0".equals(jsonObject1.getString(Common.banMobileHotspot)) ? "1" : "0";
            safetyLimitData.banShortMessage = "0".equals(jsonObject1.getString(Common.banTelephone)) ? "1" : "0";

            //safetyLimitData.machineCardBind = jsonObject1.getString( Common.machineCardBind );
            safetyLimitData.secureDesktop = jsonObject1.getString(Common.secureDesktop);
            String safetyLimitDesktops = null;
            JSONArray jsonArray = jsonObject1.getJSONArray(Common.safetyLimitDesktops);
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    String packageName = jsonArray.getJSONObject(i).getString("packageName");
                    if (safetyLimitDesktops == null) {
                        safetyLimitDesktops = packageName;
                    } else {
                        safetyLimitDesktops += "," + packageName;
                    }
                }
            }
            String displayCall = jsonObject1.getString(Common.displayCall);
            if (!TextUtils.isEmpty(displayCall) && "1".equals(displayCall)) {
                if (safetyLimitDesktops == null) {
                    safetyLimitDesktops = Common.callPackageName;
                } else {
                    safetyLimitDesktops += "," + Common.callPackageName;
                }
            }

            String displayContacts = jsonObject1.getString(Common.displayContacts);
            if (!TextUtils.isEmpty(displayContacts) && "1".equals(displayContacts)) {
                if (safetyLimitDesktops == null) {
                    safetyLimitDesktops = Common.contactsPackageName;
                } else {
                    safetyLimitDesktops += "," + Common.contactsPackageName;
                }
            }

            String displayMessage = jsonObject1.getString(Common.displayMessage);
            if (!TextUtils.isEmpty(displayMessage) && "1".equals(displayMessage)) {
                if (safetyLimitDesktops == null) {
                    safetyLimitDesktops = Common.messagePackageName;
                } else {
                    safetyLimitDesktops += "," + Common.messagePackageName;
                }
            }
            safetyLimitData.safetyLimitDesktops = safetyLimitDesktops;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.w(TAG, LogUtil.getExceptionInfo(e));
        }
        return safetyLimitData;
    }

    /**
     * 应用围栏解析
     *
     * @param extra
     * @return
     */
    public static AppFenceData jsonAppFenceData(String extra) {
        AppFenceData appFenceData = new AppFenceData();
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(extra);
            try {
                appFenceData.id = jsonObject.getString(Common.appFenceId);
            } catch (Exception e) {

            }
            JSONArray jsonArray1 = jsonObject.getJSONArray(Common.appFencePolicy);
            JSONObject jsonObject1 = jsonArray1.getJSONObject(0);
            JSONObject jsonObject2 = jsonObject1.getJSONObject(Common.applicationFence);

            appFenceData.name = jsonObject2.getString(Common.appFenceName);
            appFenceData.coordinate = jsonObject2.getString(Common.appFenceCoordinate);
            appFenceData.radius = jsonObject2.getString(Common.appFenceRadius);
            appFenceData.startDateRange = jsonObject2.getString(Common.appFenceStartDateRange);
            appFenceData.endDateRange = jsonObject2.getString(Common.appFenceEndDateRange);
            appFenceData.noticeMessage = jsonObject2.getString(Common.appFenceNoticeMessage);
            appFenceData.noticeMessageContent = jsonObject2.getString(Common.appFenceMessageContent);
            appFenceData.noticeBell = jsonObject2.getString(Common.appFenceNoticeBell);
            appFenceData.limitType = jsonObject2.getString(Common.appFenceLimitType);

            JSONArray jsonArray2 = jsonObject1.getJSONArray(Common.appFenceTimeFenceUnit);
            if (jsonArray2 != null) {
                appFenceData.timeUnit = new ArrayList<>();
                for (int i = 0; i < jsonArray2.length(); i++) {
                    AppFenceData.TimeUnitBean timeUnitBean = new AppFenceData.TimeUnitBean();
                    JSONObject jsonObject3 = jsonArray2.getJSONObject(i);
                    timeUnitBean.startTime = jsonObject3.getString(Common.appFenceStartTime);
                    timeUnitBean.endTime = jsonObject3.getString(Common.appFenceEndTime);
                    timeUnitBean.typeDate = jsonObject3.getString(Common.appFenceTypeDate);
                    timeUnitBean.unitType = jsonObject3.getString(Common.appFenceUnitType);
                    appFenceData.timeUnit.add(timeUnitBean);
                }
            }

            JSONArray jsonArray3 = jsonObject1.getJSONArray(Common.appFenceApplicationPrograms);
            if (jsonArray3 != null) {
                appFenceData.packageNames = new ArrayList<>();
                for (int i = 0; i < jsonArray3.length(); i++) {
                    appFenceData.packageNames.add(jsonArray3.getJSONObject(i).getString(Common.appFenceAppPageName));
                }
            }
        } catch (Exception e) {

        }
        return appFenceData;
    }

    /**
     * 解析设置数据
     *
     * @param extra
     */
    public static void jsonSettingData(String extra) {
        try {
            JSONObject object = new JSONObject(extra);
            String message = object.getString("message");
            final JSONObject object1 = new JSONObject(message);

            SettingAboutData settingAboutData = new SettingAboutData();
            settingAboutData.messageForHelp = object1.getString(Common.setting_help);
            settingAboutData.agreementLicense = object1.getString(Common.setting_agreement);
            settingAboutData.supportContent = object1.getString(Common.setting_stand_by);
            TheTang.getSingleInstance().storageSettingAboutData(settingAboutData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析电话白名单
     *
     * @param extra
     */
    public static List<TelephoyWhiteUser> jsonTelePhoneWhite(String extra) {
        List<TelephoyWhiteUser> list = null;
        try {
            JSONObject object = new JSONObject(extra);
            if (object != null) {
                list = new ArrayList<>();
                JSONArray jsonArray = object.getJSONArray("data");
                if (jsonArray != null && jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        TelephoyWhiteUser mTelephoyWhiteUser = new TelephoyWhiteUser();
                        JSONObject telephoywhiteuser = jsonArray.getJSONObject(i);
                        if ("null".equals(telephoywhiteuser.getString("name").trim())) {
                            mTelephoyWhiteUser.setUserName(" ");
                        } else {
                            mTelephoyWhiteUser.setUserName(telephoywhiteuser.getString("name").trim());
                        }
                        mTelephoyWhiteUser.setUserId(telephoywhiteuser.getString("id").trim());
                        if ("null".equals(telephoywhiteuser.getString("teamName").trim())) {
                            mTelephoyWhiteUser.setUserAddress("");
                        } else {
                            mTelephoyWhiteUser.setUserAddress(telephoywhiteuser.getString("teamName"));
                        }
                        if ("null".equals(telephoywhiteuser.getString("phone").trim())) {
                            mTelephoyWhiteUser.setTelephonyNumber("");
                        } else {
                            mTelephoyWhiteUser.setTelephonyNumber(telephoywhiteuser.getString("phone").trim());
                        }
                        if ("null".equals(telephoywhiteuser.getString("phone").trim())) {
                            mTelephoyWhiteUser.setLoginName("");
                        } else {
                            mTelephoyWhiteUser.setLoginName(telephoywhiteuser.getString("loginName").trim());
                        }
                        if ("null".equals(telephoywhiteuser.getString("shortPhoneNum").trim())) {
                            mTelephoyWhiteUser.setShortPhoneNum("");
                        } else {
                            mTelephoyWhiteUser.setShortPhoneNum(telephoywhiteuser.getString("shortPhoneNum").trim());
                        }
                        list.add(mTelephoyWhiteUser);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * parse sensitive_word_strategy info from json
     *
     * @param jsonString
     * @return
     */
    public static SensitiveStrategyInfo getSensitiveStategyInfo(String jsonString) {
        SensitiveStrategyInfo info = new SensitiveStrategyInfo();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            info.setId(jsonObject.getString(Common.SENSITIVE_STRATEGY_ID));
            info.setSensiWord(jsonObject.getString(Common.SENSITIVE_WORD));
            info.setStrategeName(jsonObject.getString(Common.SENSITIVE_STRATEGY_NAME));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return info;
    }

    /**
     * parse sensitive_word_strategy id from json
     *
     * @param jsonString
     * @return
     */
    public static SensitiveStrategyInfo getDeletedSensitiveStategyInfo(String jsonString) {
        SensitiveStrategyInfo info = new SensitiveStrategyInfo();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            info.setId(jsonObject.getString(Common.SENSITIVE_STRATEGY_ID));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return info;
    }

    //    {"code":169,"id":15,"name":"testqq","sendId":"1000001237882237","sensitiveStrategy":"123,456,qwe"}
    static String json = "{\"id\":\"176\",\"name\":\"aaa\",\"startDateRange\":\"2018-8-7\",\"endDateRange\":\"2018-8-8\"," +
            "\"timeUnit\":[" +
            "{unitType: 1, typeDate: \"\", startTime: \"00:00\", endTime: \"23:59\"}," +
            "{unitType: 2, typeDate: \"1\", startTime: \"00:00\", endTime: \"13:59\"}," +
            "{unitType: 3, typeDate: \"\", startTime: \"00:00\", endTime: \"23:59\"}," +
            "{unitType: \"4\", typeDate: \"2018-8-2\", startTime: \"00:00\", endTime: \"13:59\"}" +
            "]}";

    //    json {"code":176,"sendId":"1000000878428069","id":5,
    // "strategy":{"startDateRange":"2018/8/8","useNumber":0,"timeDescribe":"","createTime":1533720774000,"issuedNumber":0,"adminId":1,"name":"aaa","id":5,"saveDays":7,"endDateRange":"2018/8/17","timeFenceUnits":[{"typeDate":"","unitType":1,"startTime":"00:00","endTime":"11:59"},{"typeDate":"","unitType":3,"startTime":"14:00","endTime":"23:59"}],"timeUnit":"[{\"endTime\":\"11:59\",\"startTime\":\"00:00\",\"typeDate\":\"\",\"unitType\":1},{\"endTime\":\"23:59\",\"startTime\":\"14:00\",\"typeDate\":\"\",\"unitType\":3}]"}}
    public static SmsPolicyInfo getSmsPolicyInfo(String jsonString) {
//        jsonString = json;    //todo baii to delete
        SmsPolicyInfo info = new SmsPolicyInfo();
        if (jsonString != null && jsonString.equals("")) {
            return info;
        }
        try {
//            JSONObject strategyObject = new JSONObject(jsonString);
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONObject strategyObject = jsonObject.getJSONObject("strategy");
            info.setId(strategyObject.getString("id"));
            info.setName(strategyObject.getString("name"));
            TimeData timeData = info.getTimeData();
            timeData.setStartDateRange(strategyObject.getString("startDateRange"));
            timeData.setEndDateRange(strategyObject.getString("endDateRange"));
//            JSONArray array = strategyObject.getJSONArray("timeUnit");
            JSONArray array = strategyObject.getJSONArray("timeFenceUnits");
            if (array != null && array.length() > 0) {
                for (int i = 0; i < array.length(); i++) {
                    TimeData.TimeUnit timeUnit = new TimeData.TimeUnit();
                    JSONObject object = array.getJSONObject(i);
                    timeUnit.setUnitType(object.getString("unitType"));
                    timeUnit.setTypeDate(object.getString("typeDate"));
                    timeUnit.setStartTime(object.getString("startTime"));
                    timeUnit.setEndTime(object.getString("endTime"));
                    timeData.getTimeUnits().add(timeUnit);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return info;
    }

    public static CallRecorderPolicyInfo getCallRecorderPolicyInfo(String jsonString) {
//        jsonString = json;    //todo baii to delete
        CallRecorderPolicyInfo info = new CallRecorderPolicyInfo();
        if (jsonString != null && jsonString.equals("")) {
            return info;
        }
        try {
//            JSONObject strategyObject = new JSONObject(jsonString);
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONObject strategyObject = jsonObject.getJSONObject("strategy");
            info.setId(strategyObject.getString("id"));
            info.setName(strategyObject.getString("name"));
            TimeData timeData = info.getTimeData();
            timeData.setStartDateRange(strategyObject.getString("startDateRange"));
            timeData.setEndDateRange(strategyObject.getString("endDateRange"));
//            JSONArray array = strategyObject.getJSONArray("timeUnit");
            JSONArray array = strategyObject.getJSONArray("timeFenceUnits");
            if (array != null && array.length() > 0) {
                for (int i = 0; i < array.length(); i++) {
                    TimeData.TimeUnit timeUnit = new TimeData.TimeUnit();
                    JSONObject object = array.getJSONObject(i);
                    timeUnit.setUnitType(object.getString("unitType"));
                    timeUnit.setTypeDate(object.getString("typeDate"));
                    timeUnit.setStartTime(object.getString("startTime"));
                    timeUnit.setEndTime(object.getString("endTime"));
                    timeData.getTimeUnits().add(timeUnit);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return info;
    }
}
