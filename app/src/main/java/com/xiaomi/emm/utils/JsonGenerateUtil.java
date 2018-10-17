package com.xiaomi.emm.utils;

import com.xiaomi.emm.definition.Common;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonGenerateUtil {
    /**
     * 解析Switch Log
     * @param log
     * @return
     */
    public static String jsonSwitchLog(String log) {
        JSONObject logObject = new JSONObject(  );
        try {
            logObject.put( "alias", PreferencesManager.getSingleInstance().getData( Common.alias ) );
            JSONArray jsonArray = new JSONArray(  );
            String[] logList = log.split( "," );
            for ( int i = 0; i < logList.length; i++) {
                JSONObject switchLog = new JSONObject(  );
                String[] logString = logList[i].split( "/" );
                switchLog.put( "create_time", logString[0] );
                switchLog.put( "type", logString[1] );
                switchLog.put( "switch_direction", logString[2] );
                jsonArray.put( switchLog );
            }
            logObject.put( "list", jsonArray );
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return logObject.toString();
    }

    public String getDeviceInfoString() {
        return null;
    }
}
