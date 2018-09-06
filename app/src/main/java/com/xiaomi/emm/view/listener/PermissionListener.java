package com.xiaomi.emm.view.listener;

import java.util.List;

/**
 * Created by Administrator on 2017/8/14.
 */

public interface PermissionListener {

    public void onGranted();

    public void onDenied(List<String> permissions);
}
