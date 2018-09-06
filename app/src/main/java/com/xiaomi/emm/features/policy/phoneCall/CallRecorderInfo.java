package com.xiaomi.emm.features.policy.phoneCall;

public class CallRecorderInfo {
/*
　　address：  对方手机号，如+86138138000
    person：   发件人，如果发件人在通讯录中则为具体姓名，陌生人为null
　　date：       日期，long型，如1346988516，可以对日期显示格式进行设置
　　type：       CallLog.Calls.TYPE
　　path：      录音保存位置
　　duration：通话时长 */

    private String mAddress;
    private String mPerson;
    private String mDate;
    private long mDuration;
    private int mType;
    private String mPath;

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        this.mAddress = address;
    }

    public String getPerson() {
        return mPerson;
    }

    public void setPerson(String person) {
        this.mPerson = person;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        this.mDate = date;
    }

    public long getmDuration() {
        return mDuration;
    }

    public void setDuration(long duration) {
        this.mDuration = duration;
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String path) {
        this.mPath = path;
    }

    @Override
    public String toString() {
        return "CallRecorderInfo " + "name " + mPerson
                + ", num " + mAddress
                + ", duration " + mDuration
                +", type " + mType
                + ", time " + mDate;
    }
}
