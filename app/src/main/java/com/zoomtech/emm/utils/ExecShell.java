package com.zoomtech.emm.utils;

/**
 * Created by Administrator on 2017/8/16.
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * shell命令执行
 */
class ExecShell {

    private final static String TAG = "ExecShell";

    public static enum SHELL_CMD {
        check_su_binary(new String[]{"/system/xbin/which", "su"});

        String[] command;

        SHELL_CMD(String[] command) {
            this.command = command;
        }

    }

    /**
     * 执行shell命令
     *
     * @param shellCmd {@link SHELL_CMD#SHELL_CMD(String[])}
     * @return
     */
    public static ArrayList<String> executeCommand(SHELL_CMD shellCmd) {

        String line = null;
        ArrayList<String> fullResponse = new ArrayList<String>();

        Process localProcess = null;

        try {
            localProcess = Runtime.getRuntime().exec(shellCmd.command);
        } catch (Exception e) {
            return null;
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(localProcess.getInputStream()));

        try {
            while ((line = in.readLine()) != null) {
                LogUtil.writeToFile(TAG, "–> Line received: " + line);
                fullResponse.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        LogUtil.writeToFile(TAG, "–> Full response was: " + fullResponse);

        return fullResponse;
    }
}
