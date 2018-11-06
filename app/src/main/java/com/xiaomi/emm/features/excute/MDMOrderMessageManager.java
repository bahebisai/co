package com.xiaomi.emm.features.excute;

import android.text.TextUtils;
import android.util.Log;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.features.presenter.TheTang;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by lenovo on 2018/1/2.
 */

public class MDMOrderMessageManager {
    public final static String TAG = "MDMOrderMessageManager";
    public static ConcurrentMap<String,String> messageMap = new ConcurrentHashMap<>(  );
    //单例
    private volatile static MDMOrderMessageManager mMDMOrderMessageManager;

    private MDMOrderMessageManager() {
    }

    /**
     * 单例
     *
     * @return
     */
    public static MDMOrderMessageManager getSingleInstance() {
        if (null == mMDMOrderMessageManager) {
            synchronized (MDMOrderMessageManager.class) {
                if (null == mMDMOrderMessageManager) {
                    mMDMOrderMessageManager = new MDMOrderMessageManager();
                }
            }
        }
        return mMDMOrderMessageManager;
    }

    /**
     * 存储接收到的命令
     * @param extra
     * @param sendId
     */
    public void feedBackMessage(String extra, String sendId) {

        LogUtil.writeToFile(TAG,"extra: " + extra);
        Log.w(TAG,"----------==" + extra);
        if (extra.equals("{}") || extra == null || extra.length() == 0 ) {
            LogUtil.writeToFile(TAG,"extra is null!");
            return;
        }

        addOrderMessage( extra, sendId );
        feedbackMessage( extra, sendId );
    }

    /**
     * 添加命令消息
     * @param extra
     * @param sendId
     */
    private void addOrderMessage(String extra, String sendId) {
        messageMap.put( sendId, extra );
    }

    /**
     * 获取命令消息
     * @param sendId
     * @return
     */
    private String getOrderMessage(String sendId) {
        return messageMap.get( sendId );
    }

    /**
     * 删除命令消息
     * @param sendId
     */
    private void deleteOrderMessage( String sendId ) {
        messageMap.remove( sendId );
    }

    /**
     * 消息反馈
     * @param extra
     */
    public void feedbackMessage(String extra, String sendId ) {
        LogUtil.writeToFile( TAG, "feedbackMessage = " + extra);
        Log.w( TAG, "feedbackMessage = " + extra);
        MDMOrderMessageBackImpl mDMOrderMessageBackImpl = new MDMOrderMessageBackImpl();
    }

    /**
     * 消息反馈成功后调用
     * @param sendId
     */
    public void feedbackSuccess(String sendId) {
        LogUtil.writeToFile( TAG, "feedbackSuccess = " + sendId);
        Log.w( TAG, "feedbackSuccess = " + sendId);
        String extra = getOrderMessage( sendId );

        LogUtil.writeToFile( TAG, "feedbackSuccess extra = " + extra);

        if (TextUtils.isEmpty( extra ))
            return;

        excuteOrder(extra);

        deleteOrderMessage( sendId );
    }

    /**
     * 消息反馈失败后调用,
     * @param sendId
     */
    public void feedbackFail(String sendId) {
        deleteOrderMessage(sendId);
    }

    /**
     * 执行命令
     * @param extra
     */
    public void excuteOrder(String extra) {
        LogUtil.writeToFile( TAG, "excuteOrder = " + extra);
        Log.w( TAG, "excuteOrder = " + extra);
        MDMOrderExcuting mdmOrderExcuting = new MDMOrderExcuting();
        mdmOrderExcuting.processOrderMessage( TheTang.getSingleInstance().getContext(), extra );
    }
}
