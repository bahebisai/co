package com.xiaomi.emm.features.resend;

import com.xiaomi.emm.features.db.DatabaseOperate;
import com.xiaomi.emm.model.MessageResendData;
import com.xiaomi.emm.utils.LogUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/10/27.
 * MessageResendManager重发反馈
 */

public class MessageResendManager implements Runnable {

    /**
     * 0：初始
     * 1：成功
     * 2：错误
     * 3：失败
     * 4：表示已经发送，等待结果
     */
    int type = 0;

    public static final String TAG = "MessageResendManager";

    public MessageResendListener mMessageResendListener = null;

    public List<String> message_ids = new ArrayList<>();

    @Override
    public void run() {
        excute();
    }

    /**
     *synchronized 防止线程冲突
     */
    private synchronized void excute() {

        LogUtil.writeToFile( TAG, "MessageResendManager excute!" );

        mMessageResendListener = new MessageResendListener();

        List<MessageResendData> lists = new ArrayList<>();

        lists = DatabaseOperate.getSingleInstance().queryAll_backResult_sql(); //获取全部未发送成功的反馈

        if (lists == null || lists.size() == 0) {
            LogUtil.writeToFile( TAG, "there is no failed message about feedback!" );
            return;
        }

        LogUtil.writeToFile( TAG, "resend lists size = " + lists.size() );
        LogUtil.writeToFile( TAG, "resend lists content = " + lists.toString() );

        //可能有问题
        for ( MessageResendData messageResendData : lists) {
            if (!resendMessage(messageResendData))  //如果发送失败则默认网络不通，关闭，等待下一次发送
            {
                break;
            }
        }

        DatabaseOperate.getSingleInstance().delete_id_backResult_sql(message_ids);
    }

    /**
     * 重发回调
     */
    class MessageResendListener implements ResendListener {

        @Override
        public void resendSuccess() {
            type = 1; //成功
        }

        @Override
        public void resendError() {
            type = 2; //错误
        }

        @Override
        public void resendFail() {
            type = 3; //失败
        }
    }

    /**
     * 发送信息
     * @param messageResendData
     * @return
     */
    public boolean resendMessage(MessageResendData messageResendData) {

        boolean result = false;

        type = 0;

        while (true) {
            if ( type == 0 ) {
                type = 4;
                resend(messageResendData,mMessageResendListener); //发送
            } else if ( type == 1 ) {
                message_ids.add( messageResendData.resend_id );
                LogUtil.writeToFile( TAG, messageResendData.resend_id + " is success!");
                result = true;
                break;
            } else if ( type == 2 ){ //目前发送错误与发送失败一致
                result = false;
                break;
            } else if ( type == 3 ){
                result = false;
                break;
            }
        }

        return result;
    }

    /**
     * 发送
     * @param messageResendData
     * @param mMessageResendListener
     */
    private void resend(MessageResendData messageResendData, MessageResendListener mMessageResendListener) {

        LogUtil.writeToFile( TAG, "resend " + messageResendData.resend_type + "," + " content " + messageResendData.resend_content );

        MessageResendTask mMessageResendTask = new MessageResendTask(messageResendData, mMessageResendListener);
        mMessageResendTask.resendBack();
    }

    /**
     * 重发监听
     */
    public interface ResendListener {
        public void resendSuccess() ;
        public void resendError();
        public void resendFail();
    }
}
