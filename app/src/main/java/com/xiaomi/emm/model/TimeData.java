package com.xiaomi.emm.model;

import java.util.ArrayList;
import java.util.List;


   /*"timeUnit":[{"endTime":"23:59","startTime":"12:00","typeDate":"","unitType":3}
    "startTimeRage":"2018-8-1"
            "endTimeRage":"2018-8-1"*/
/**
 * 短信备份、录音备份都会设置时间段
 * 这个类共享时间段管理判断
 */
public class TimeData {
    private String mStartDateRange = "2018-8-1";
    private String mEndDateRange;
    private List<TimeUnit> mTimeUnits;

    public String getStartDateRange() {
        return mStartDateRange;
    }

    public void setStartDateRange(String startDateRange) {
        this.mStartDateRange = startDateRange;
    }

    public String getEndDateRange() {
        return mEndDateRange;
    }

    public void setEndDateRange(String endDateRange) {
        this.mEndDateRange = endDateRange;
    }

    public TimeData() {
        mTimeUnits = new ArrayList<>();
    }

    public List<TimeUnit> getTimeUnits() {
        return mTimeUnits;
    }

    public void setTimeUnits(List<TimeUnit> timeUnits) {
        this.mTimeUnits = timeUnits;
    }

    public static class TimeUnit {
        /**
         * json
         * endTime : 23:59
         * startTime : 00:00
         * typeDate :
         * unitType : 1
         */
        private String typeDate;
        private String unitType;
        private String endTime;//hhmm
        private String startTime;//hhmm


        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getTypeDate() {
            return typeDate;
        }

        public void setTypeDate(String typeDate) {
            this.typeDate = typeDate;
        }

        public String getUnitType() {
            return unitType;
        }

        public void setUnitType(String unitType) {
            this.unitType = unitType;
        }

        @Override
        public String toString() {
            return "{" +
                    "startTime='" + startTime + '\'' +
                    ", endTime='" + endTime + '\'' +
                    ", typeDate='" + typeDate + '\'' +
                    ", unitType='" + unitType + '\'' +
                    '}' + "\n";
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (TimeUnit timeUnit:mTimeUnits) {
            stringBuilder.append(timeUnit.toString());
        }
        return stringBuilder.toString();
    }
}
