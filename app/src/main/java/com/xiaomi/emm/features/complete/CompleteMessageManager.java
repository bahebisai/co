package com.xiaomi.emm.features.complete;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import com.xiaomi.emm.definition.OrderConfig;
import com.xiaomi.emm.features.db.DatabaseOperate;
import com.xiaomi.emm.model.CompleteMessageData;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.utils.TheTang;
import java.util.List;

/**
 * Created by Administrator on 2018/1/13.
 */

public class CompleteMessageManager {

    public final static String TAG = "CompleteMessageManager";

    //发送消息
    public synchronized void sendCompleteMessage(String type, String result, String id) {
        //定时器到时执行发送时传入type为null
        if (type == null) {
            sendMessageByTime();
        } else {
            sendMessageByResult( type, result, id );
        }
    }

    /**
     * 通过定时器来发送执行失败或发送不成功的的消息（1分钟）
     */
    private void sendMessageByTime() {

        List<CompleteMessageData> mList = queryAllMessageResult();

        if (mList != null && mList.size() > 0) {
            for (CompleteMessageData mCompleteMessageData : mList) {
                //状态为true（为发送失败的） 或者 超时都发送
                if ("true".equals( mCompleteMessageData.result )) {
                    sendMessage( mCompleteMessageData.type, mCompleteMessageData.result, mCompleteMessageData.id );
                } else {
                    //为-1时，不发送
                    if ("-1".equals( mCompleteMessageData.time )) {
                        return;
                    }
                    //超时1min
                    if ((System.currentTimeMillis() - Double.valueOf( mCompleteMessageData.time )) > 1 * 60 * 1000) {
                        sendMessage( mCompleteMessageData.type, mCompleteMessageData.result, mCompleteMessageData.id );
                    }
                }
            }
        }
    }

    /**
     * 通过结果来发送执行成功的消息（时间不确定或者1分钟内）
     *
     * @param type
     */
    private void sendMessageByResult(String type, String result, String id) {

        //如果sendId为空，不能返回，因为命令也经不存在
        if (TextUtils.isEmpty( id ))
            return;

        CompleteMessageData mCompleteMessageData = queryMessageResult( type, id );

        //判断数据库中是否存在该命令
        if (mCompleteMessageData == null || mCompleteMessageData.id == null)
            return;

        if (String.valueOf( OrderConfig.SilentInstallAppication ).equals( type )) {
            //当下载参数或后台参数返回失败时，将结果直接返回
            if ("-1".equals( mCompleteMessageData.time )) {

                updateMessageResultAndTime( type, "false", String.valueOf( System.currentTimeMillis() ), mCompleteMessageData.id );

                if ("true".equals( result )) {
                    return;
                }
            }
        } else {

            if ("true".equals( result )) {
                updateMessageResult( mCompleteMessageData.type, "true", mCompleteMessageData.id );
            }

        }

        sendMessage( mCompleteMessageData.type, result, mCompleteMessageData.id );
    }

    /**
     * 发送消息
     *
     * @param type
     * @param result
     */
    private void sendMessage(String type, String result, String id) {
        TheTang.getSingleInstance().sendExcuteComplete( new MessagesendListener(), type, result, id );
    }

    /**
     * 发送成功调用
     *
     * @param type
     */
    public void sendMessageSuccess(String type, String id) {
        deleteMessageByType( type, id );
    }

    /**
     * 读取消息结果
     *
     * @param type
     * @return
     */
    private CompleteMessageData queryMessageResult(String type, String id) {
        CompleteMessageData mCompleteMessageData = DatabaseOperate.getSingleInstance().queryCompleteResultSql( type, id );
        return mCompleteMessageData;
    }

    private List<CompleteMessageData> queryAllMessageResult() {
        List<CompleteMessageData> mList = DatabaseOperate.getSingleInstance().queryAllCompleteResultSql();
        return mList;
    }

    public void addMessageResult(String type, String result, String time, String id) {
        DatabaseOperate.getSingleInstance().addCompleteResult( type, result, time, id );
    }

    private void updateMessageResult(String type, String result, String id) {
        DatabaseOperate.getSingleInstance().updateCompleteResult( type, result, id );
    }

    private void updateMessageResultAndTime(String type, String result, String time, String id) {
        DatabaseOperate.getSingleInstance().updateCompleteResultAndTime( type, result, time, id );
    }
    private void deleteMessageByType(String type, String id) {
        DatabaseOperate.getSingleInstance().deleteCompleteResultSql( type, id );

        checkWhetherHadOrder(0);
    }

    /**
     * type == 0 删除
     * type == 1 添加
     * @param type
     */
    public synchronized static void checkWhetherHadOrder(int type) {

        if (type == 0) {//删除
            List<CompleteMessageData> mList = DatabaseOperate.getSingleInstance().queryAllCompleteResultSql();

            if (mList == null || mList.size() < 1) {
                AlarmManager alarmManager = (AlarmManager) TheTang.getSingleInstance().getSystemService(Context.ALARM_SERVICE);
                Intent intent1 = new Intent();
                intent1.setAction("timer_task");
                //第二个参数用于识别AlarmManager
                PendingIntent pendingIntent = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 4, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.cancel(pendingIntent);
            }
        } else if (type == 1) {//添加
            boolean time_out = false;

            List<CompleteMessageData> mList = DatabaseOperate.getSingleInstance().queryAllCompleteResultSql();

            //用于判断闹钟是否存在
            if (mList != null && mList.size() > 0) {
                for (CompleteMessageData mCompleteMessageData : mList) {
                    if (mCompleteMessageData.time != "-1" && (System.currentTimeMillis() - Double.valueOf(mCompleteMessageData.time)) >= 4 * 60 * 1000) {
                        time_out = true;
                    }
                }
            }

            if (!time_out)
                return;

            AlarmManager alarmManager = (AlarmManager) TheTang.getSingleInstance().getSystemService(Context.ALARM_SERVICE);
            Intent intent1 = new Intent();
            intent1.setAction("timer_task");
            //第二个参数用于识别AlarmManager
            PendingIntent pendingIntent = PendingIntent.getBroadcast(TheTang.getSingleInstance().getContext(), 4, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 2 * 60 * 1000, pendingIntent);

        }
    }

    /**
     * 发送监听
     */
    class MessagesendListener implements SendListener {

        @Override
        public void sendSuccess(String type, String id) {
            sendMessageSuccess( type, id );
            Log.w( TAG, "执行完成消息发送成功 type = " + type );
            LogUtil.writeToFile( TAG, "执行完成消息发送成功 type = " + type );
        }

        @Override
        public void sendFail(String type, String id) {
            Log.w( TAG, "执行完成消息发送失败 type = " + type );
            LogUtil.writeToFile( TAG, "执行完成消息发送失败 type = " + type );
        }
    }

    /**
     * 发送监听
     */
    public interface SendListener {
        public void sendSuccess(String type, String id);

        public void sendFail(String type, String id);
    }
}
