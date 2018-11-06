package com.xiaomi.emm.features.excute;

import android.content.Context;

import com.xiaomi.emm.base.BaseApplication;
import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.definition.OrderConfig;
import com.xiaomi.emm.features.manager.CompleteMessageManager;
import com.xiaomi.emm.features.db.DatabaseOperate;
import com.xiaomi.emm.features.impl.SendMessageManager;
import com.xiaomi.emm.features.policy.container.ContainerStratege;
import com.xiaomi.emm.features.policy.device.ConfigurationPolicy;
import com.xiaomi.emm.model.DownLoadEntity;
import com.xiaomi.emm.model.MessageSendData;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.features.presenter.MDM;
import com.xiaomi.emm.features.manager.PreferencesManager;
import com.xiaomi.emm.features.presenter.TheTang;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class OrderFeedbackManager {
    public static final String TAG = OrderFeedbackManager.class.getName();
    private Context mContext;

    public OrderFeedbackManager() {
        mContext = TheTang.getSingleInstance().getContext();
    }

    /***********************************
     * 执行反馈
     ***************************************************/
    public void sendExcuteComplete(CompleteMessageManager.SendListener listener, String code, String result, String id) {//todo baii util http
/*        if (mExcuteCompleteImpl == null)
            mExcuteCompleteImpl = new ExcuteCompleteImpl(mContext);
        mExcuteCompleteImpl.sendExcuteComplete(listener, code, result, id);*/
//todo impl bai 5555555555
        final JSONObject completeObject = new JSONObject(  );
        try {
            completeObject.put( "alias", PreferencesManager.getSingleInstance().getData( Common.alias ) );
            completeObject.put( "code", code );
            completeObject.put( "result", result );
            completeObject.put( "sendId", id );
            LogUtil.writeToFile( TAG, completeObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        MessageSendData data = new MessageSendData(Common.excute_complete_impl, completeObject.toString(), false);
        SendMessageManager manager = new SendMessageManager(mContext);
        manager.setSendListener(new SendMessageManager.SendListener() {
            @Override
            public void onSuccess() {
                listener.sendSuccess(code, id);
                exeOnSendSuccess(code, id);
            }

            @Override
            public void onFailure() {
                listener.sendFail(code, id);
            }

            @Override
            public void onError() {
                listener.sendFail(code, id);
            }
        });
        manager.sendMessage(data);
    }

    private void exeOnSendSuccess(String code, String id) {
        //关机
        if (String.valueOf(OrderConfig.SetShutDown).equals(code)) {
            MDM.setShutDown();
        }

        //重启
        if (String.valueOf(OrderConfig.SetReboot).equals(code)) {
            MDM.setReboot(code);
        }

        //擦除
        if (String.valueOf(OrderConfig.WipeData).equals(code)) {
            MDM.wipeData();
        }

        //恢复出厂
        if (String.valueOf(OrderConfig.SetFactoryReset).equals(code)) {
            MDM.setFactoryReset(/*order*/);
        }

        //删除用户
        if (String.valueOf(OrderConfig.login_out_and_delete_data).equals(code)) {
            MDM.deleteAccount(/*order*/);
        }

        //切换到生活域
        if (String.valueOf(OrderConfig.TOLifeContainer).equals(code)) {
            MDM.toLifeContainer();
        }

        //切换到安全域
        if (String.valueOf(OrderConfig.TOSecurityContainer).equals(code)) {
            MDM.toSecurityContainer();
        }

        //进入安全区域
        if (String.valueOf(OrderConfig.enter_sercurity_stratege).equals(code)) {
            PreferencesManager.getSingleInstance().setSecurityData(Common.safetyTosecureFlag, "true");
            ContainerStratege.excuteSecurityContainerStratege();
        }

        //进入生活区域
        if (String.valueOf(OrderConfig.enter_life_stratege).equals(code)) {
            PreferencesManager.getSingleInstance().removeSecurityData(Common.safetyTosecureFlag);
            ContainerStratege.excuteLifeContainerStratege();
        }

        //在静默安装命令完成时，应该清除相关数据(防止应用安装失败，无法清除相关数据)
        if (String.valueOf(OrderConfig.SilentInstallAppication).equals(code)) {

            DownLoadEntity mDownLoadEntity = DatabaseOperate.getSingleInstance().queryDownLoadFileBySendId(id);

            if (mDownLoadEntity != null) {
                MDM.deleteFile(new File(BaseApplication.baseAppsPath + File.separator + mDownLoadEntity.saveName));
                DatabaseOperate.getSingleInstance().deleteDownLoadFile(mDownLoadEntity);
            }
        }
        if (String.valueOf(OrderConfig.send_configure_Strategy).equals(code)) {
            //这里主要是存储配置策略的数据后给后台返回再执行，因为先执行会导致wifi断开执行返回一直发送失败
            String extra = PreferencesManager.getSingleInstance().getConfiguration("json_Configurat");
            ConfigurationPolicy.excuteConfigurationPolicy(extra);
        }


        if (String.valueOf(OrderConfig.delete_configure_Strategy).equals(code)) {
            //这里主要是存储配置策略的数据后给后台返回再执行，因为先执行会导致wifi断开执行返回一直发送失败
            ConfigurationPolicy.deleteConfigurationPolicy();
        }
    }
}
