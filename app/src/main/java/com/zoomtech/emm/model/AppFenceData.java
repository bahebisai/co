package com.zoomtech.emm.model;

import java.util.List;

/**
 * Created by Administrator on 2018/1/16.
 */

public class AppFenceData {
    public String id;
    public String name;
    public String coordinate;
    public String radius;
    public String startDateRange;
    public String endDateRange;
    public String noticeMessage;
    public String noticeBell;
    public String limitType;
    public String noticeMessageContent;
    public List<TimeUnitBean> timeUnit;
    public List<String> packageNames;
    public static class TimeUnitBean {
        /**
         * endTime : 23:59
         * startTime : 00:00
         * typeDate :
         * unitType : 1
         */
        public String typeDate;
        public String unitType;
        public String endTime;
        public String startTime;
    }

    }
