package com.xiaomi.emm.model;

/**
 * Created by lenovo on 2017/9/22.
 */

public class StrategyFeedBackData  {

    private String id;
    private String alias;
    private String type;
    private String feedback_code;
    private String result;
    private String sendId;

    public String getFeedback_code() {
        return feedback_code;
    }

    public void setFeedback_code(String feedback_code) {
        this.feedback_code = feedback_code;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }



    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getSendId() {
        return sendId;
    }

    public void setSendId(String sendId) {
        this.sendId = sendId;
    }
}
