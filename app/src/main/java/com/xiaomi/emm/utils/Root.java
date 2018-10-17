package com.xiaomi.emm.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017/8/16.
 */

public class Root {//todo baii util shell or device

    private final static String TAG = "Root";

    public static boolean isDeviceRooted() {

        if (checkRootMethod1()) {
            return true;
        }

        if (checkRootMethod2()) {
            return true;
        }

        if (checkRootMethod3()) {
            return true;
        }

        return false;
    }

    public static boolean checkRootMethod1() {
        String buildTags = android.os.Build.TAGS;

        if (buildTags != null && buildTags.contains( "test-keys" )) {
            return true;
        }
        return false;
    }

    public static boolean checkRootMethod2() {

        try {
            File file = new File( "/system/app/Superuser.apk" );
            if (file.exists()) {
                return true;
            }
        } catch (Exception e) {
        }

        return false;
    }

    public static boolean checkRootMethod3() {

        if (new ExecShell().executeCommand( ExecShell.SHELL_CMD.check_su_binary ) != null) {
            return true;
        } else {
            return false;
        }

    }
}

