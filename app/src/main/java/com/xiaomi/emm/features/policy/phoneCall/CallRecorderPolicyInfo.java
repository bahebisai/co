package com.xiaomi.emm.features.policy.phoneCall;

import com.xiaomi.emm.model.TimeData;

public class CallRecorderPolicyInfo {
    private String mId;
    private String mName;
    private TimeData mTimeData;

    public CallRecorderPolicyInfo() {
        mTimeData = new TimeData();
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public TimeData getTimeData() {
        return mTimeData;
    }

    public void setTimeData(TimeData timeData) {
        this.mTimeData = mTimeData;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        String date = "时间范围" + mTimeData.getStartDateRange() + " - " + mTimeData.getEndDateRange();
        stringBuilder.append(date).append("\n").append("\n时间单元：\n").append(mTimeData.toString());
        return stringBuilder.toString();
    }
}
