package com.zoomtech.emm.view.listener;

import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.zoomtech.emm.features.QR.utils.CommonUtil;
import com.zoomtech.emm.features.QR.zxing.activity.CaptureActivity;
import com.zoomtech.emm.features.manager.PreferencesManager;
import com.zoomtech.emm.view.activity.LoginActivity;
import com.zoomtech.emm.R;

/**
 * Created by Administrator on 2017/7/6.
 */

public class LoginListener extends Listener {


    LoginActivity activity;

    public LoginListener(LoginActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onClickView(View view) {
        switch (view.getId()) {
            case R.id.login:
                String userName = ((EditText) activity.mViewHolder.get(R.id.username)).getText().toString();
                String passWord = ((EditText) activity.mViewHolder.get(R.id.password)).getText().toString();
                String ipAddress = ((EditText) activity.mViewHolder.get(R.id.ipAddress)).getText().toString();
                String ipPort = ((EditText) activity.mViewHolder.get(R.id.ipPort)).getText().toString();
                //String transfer = ((Spinner) activity.mViewHolder.get(R.id.transfer_spinner)).getSelectedItem().toString();

                if (ipAddress.isEmpty()) {
                    Toast.makeText(activity, "请输入Ip地址！", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (ipPort.isEmpty()) {
                    Toast.makeText(activity, "请输入端口号！", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (userName.isEmpty()) {
                    Toast.makeText(activity, "请输入用户名！", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (passWord.isEmpty()) {
                    Toast.makeText(activity, "请输入密码！", Toast.LENGTH_SHORT).show();
                    return;
                }

                PreferencesManager.getSingleInstance().setData( "baseUrl", "https://" + ipAddress + ":" + ipPort + "/" );
                activity.login(userName, passWord);
                break;
            case R.id.QR_code:
                if (CommonUtil.isCameraCanUse()) {
                    Intent intent = new Intent(activity, CaptureActivity.class);
                    activity.startActivityForResult(intent, activity.REQUEST_CODE);
                } else {
                    Toast.makeText(activity, "请打开此应用的摄像头权限！", Toast.LENGTH_SHORT).show();
                }
                break;

                default:
                    break;
        }

    }
}
