package com.xiaomi.emm.features.policy.sensitiveWords;

import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import com.xiaomi.emm.definition.OrderConfig;
import com.xiaomi.emm.features.db.DatabaseOperate;
import com.xiaomi.emm.features.excute.XiaomiMDMController;
import com.xiaomi.emm.model.SensitiveStrategyInfo;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.utils.TheTang;

import java.util.HashMap;

public class SensiWordManager {

    static final String TAG = "Sensitive Word";
    private SensitiveStrategyInfo mInfo;
    private HashMap<String, SensitiveStrategyInfo> mWordStrategyMap;

    private static class SingletonHolder {
        public static SensiWordManager instance = new SensiWordManager();
    }

    private SensiWordManager() {
        initWordStrategyMap();
    }

    public static SensiWordManager newInstance() {
        return SingletonHolder.instance;
    }

    public HashMap getWordStrategyMap() {
        return mWordStrategyMap;
    }

    private void initWordStrategyMap() {
        mWordStrategyMap = DatabaseOperate.getSingleInstance().querySensitiveWordAll();
    }

    public void addSensitiveStrategy(Context context, SensitiveStrategyInfo info) {
        if (DatabaseOperate.getSingleInstance().insertSensitiveWord(info)) {
            enableAccessibilityService(context);
//            Log.d("baii", "insert word " + info.getStrategeName());
            LogUtil.writeToFile(TAG, "insert word " + info.getStrategeName());
            mWordStrategyMap.put(info.getId(), info);
            if (mOnStrategyChangeListener != null) {
                mOnStrategyChangeListener.onStrategyAdded();
            }
            TheTang.getSingleInstance().addMessage(String.valueOf(OrderConfig.SEND_SENSITIVE_WORD_POLICY), info.getStrategeName());
            TheTang.getSingleInstance().addStratege(String.valueOf(OrderConfig.SEND_SENSITIVE_WORD_POLICY), info.getStrategeName(), System.currentTimeMillis() + "");
        }
    }

    public void deleteSensitiveStrategy(Context context, SensitiveStrategyInfo info) {
        if (null == mWordStrategyMap.get(info.getId())) {
            return;
        }
        String strategyName = mWordStrategyMap.get(info.getId()).getStrategeName();
        DatabaseOperate.getSingleInstance().deleteSensitiveWord(info);//db
        mWordStrategyMap.remove(info.getId());//hashmap
        if (mOnStrategyChangeListener != null) {
            mOnStrategyChangeListener.onStrategyDeleted();
        }
        disableAccessibilityService(context);
        TheTang.getSingleInstance().addMessage(String.valueOf(OrderConfig.DELETE_SENSITIVE_WORD), strategyName);
        TheTang.getSingleInstance().deleteStrategeInfo(String.valueOf(OrderConfig.SEND_SENSITIVE_WORD_POLICY), strategyName);
    }

    public String getSensitiveWords() {
        StringBuilder stringBuilder = new StringBuilder();
//        String[] sensiWords;
        String words;
        for (String key : mWordStrategyMap.keySet()) {
            stringBuilder.append(mWordStrategyMap.get(key).getSensiWord()).append(",");
        }
//        sensiWords = stringBuilder.toString().split(",");
        words = stringBuilder.toString();
        LogUtil.writeToFile(TAG, "getSensitiveWords " + stringBuilder.toString());
        return words;
    }

    private onStrategyChangeListener mOnStrategyChangeListener;

    public interface onStrategyChangeListener {
        void onStrategyAdded();

        void onStrategyDeleted();
    }

    public void setStrategyChangeListener(onStrategyChangeListener listener) {
        mOnStrategyChangeListener = listener;
    }

    public void enableAccessibilityService(Context context) {
        ComponentName componentName = new ComponentName(context, WordsDetectService.class);
//        if (!XiaomiMDMController.getSingleInstance().isAccessibilityServiceEnable(componentName)) {
            XiaomiMDMController.getSingleInstance().setAccessibilityService(componentName, true);
//        }
    }
    //todo bai
    public void disableAccessibilityService(Context context) {
        ComponentName componentName = new ComponentName(context, WordsDetectService.class);
        if (getSensitiveWords().trim().isEmpty() /*&& XiaomiMDMController.getSingleInstance().isAccessibilityServiceEnable(componentName)*/) {
            XiaomiMDMController.getSingleInstance().setAccessibilityService(componentName, false);
        }
    }
}
