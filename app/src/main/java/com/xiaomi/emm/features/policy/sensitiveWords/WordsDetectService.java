package com.xiaomi.emm.features.policy.sensitiveWords;

import android.accessibilityservice.AccessibilityService;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.xiaomi.emm.utils.LogUtil;

import java.util.List;

public class WordsDetectService extends AccessibilityService {
    private static String mWords;
    private static String[] mSensitiveWords;
    private SensiWordManager mWordManager;

    public WordsDetectService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        Log.d("baii", "words detect service oncreate");
        LogUtil.writeToFile(SensiWordManager.TAG, "words detect service oncreate");
        mWordManager = SensiWordManager.newInstance();
        mWordManager.setStrategyChangeListener(new SensiWordManager.onStrategyChangeListener() {
            @Override
            public void onStrategyAdded() {
                mWords = mWordManager.getSensitiveWords();
                mSensitiveWords = mWords.split(",");
            }

            @Override
            public void onStrategyDeleted() {
                mWords = mWordManager.getSensitiveWords();
                mSensitiveWords = mWords.split(",");
            }
        });
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        mWords = mWordManager.getSensitiveWords();
        mSensitiveWords = mWords.split(",");
//        Log.d("baii", "connected " + mWords);
        LogUtil.writeToFile(SensiWordManager.TAG, "connected " + mWords);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                if (null == mSensitiveWords || mSensitiveWords.length == 0) {
//                    Toast.makeText(getApplicationContext(), "no sensitive words", Toast.LENGTH_SHORT);
                    return;
                }
                String text;
                text = event.getText().toString();
                text = text.substring(1, text.length() - 1);//text contains [] which we should remove
                for (String word : mSensitiveWords) {
                    if (text.contains(word.trim())) {

                        text = text.replace(word, "***");
                        findAndPerformAction("android.widget.EditText", word, text);
                    }
                }
                break;
            default:
                break;
        }
    }

    /*********************SET TEXT OF EDITTEXT********************/
    /**
     * 填充文本
     */
    private void fillText(AccessibilityNodeInfo node, String reply) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bundle args = new Bundle();
            args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    reply);
            node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
        } else {
            ClipData data = ClipData.newPlainText("reply", reply);
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.setPrimaryClip(data);
            node.performAction(AccessibilityNodeInfo.ACTION_FOCUS); // 获取焦点
            node.performAction(AccessibilityNodeInfo.ACTION_PASTE); // 执行粘贴
        }
    }

    //找到含有敏感字的EditText
    private void findAndPerformAction(String widget, String sensiWord, String text) {
        // 取得当前激活窗体的根节点
        if (getRootInActiveWindow() == null) {
            return;
        }
        // 通过文本找到当前的节点
        List<AccessibilityNodeInfo> nodes = getRootInActiveWindow().findAccessibilityNodeInfosByText(sensiWord);
        if (nodes != null) {
            for (AccessibilityNodeInfo node : nodes) {
                if (node.getClassName().equals(widget) && node.isEnabled()) {
                    //node.performAction(AccessibilityNodeInfo.ACTION_CLICK); // 执行点击
                    fillText(node, text);
                    break;
                }
            }
        }
    }
    /*********************SET TEXT OF EDITTEXT********************/

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
