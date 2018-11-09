package com.zoomtech.emm.model;

/*
 *  @项目名：  MDM 
 *  @包名：    com.vivo.emm.model
 *  @文件名:   TrajectoryData
 *  @创建者:   lenovo
 *  @创建时间:  2018/8/9 17:02
 *  @描述：    TODO
 */

import java.util.List;

public class TrajectoryData {


    /**
     * code : 174
     * id : 2
     * sendId : 1000000309195890
     * strategy : {"adminId":1,"createTime":1533801595000,"endDateRange":"2018/8/10","frequency":1,"id":2,"issuedNumber":0,"name":"新测试","saveDays":7,"startDateRange":"2018/8/9","timeDescribe":"","timeFenceUnits":[{"endTime":"23:59","startTime":"00:00","typeDate":"","unitType":1},{"endTime":"23:59","startTime":"00:00","typeDate":"","unitType":1},{"endTime":"23:59","startTime":"00:01","typeDate":"1","unitType":2},{"endTime":"23:59","startTime":"00:00","typeDate":"2","unitType":2},{"endTime":"05:02","startTime":"03:00","typeDate":"","unitType":3},{"endTime":"23:59","startTime":"08:00","typeDate":"2018/8/10","unitType":4}],"useNumber":0}
     */

    public String id;
    public StrategyBean strategy;

    public static class StrategyBean {
        /**
         * adminId : 1
         * createTime : 1533801595000
         * endDateRange : 2018/8/10
         * frequency : 1
         * id : 2
         * issuedNumber : 0
         * name : 新测试
         * saveDays : 7
         * startDateRange : 2018/8/9
         * timeDescribe :
         * timeFenceUnits : [{"endTime":"23:59","startTime":"00:00","typeDate":"","unitType":1},{"endTime":"23:59","startTime":"00:00","typeDate":"","unitType":1},{"endTime":"23:59","startTime":"00:01","typeDate":"1","unitType":2},{"endTime":"23:59","startTime":"00:00","typeDate":"2","unitType":2},{"endTime":"05:02","startTime":"03:00","typeDate":"","unitType":3},{"endTime":"23:59","startTime":"08:00","typeDate":"2018/8/10","unitType":4}]
         * useNumber : 0
         */


        public String endDateRange;
        public String frequency;
        public String id;
        public int issuedNumber;
        public String name;
        public String startDateRange;
        public String timeDescribe;
        public String useNumber;
        public List<TimeFenceUnitsBean> timeFenceUnits;

        public static class TimeFenceUnitsBean {
            /**
             * endTime : 23:59
             * startTime : 00:00
             * typeDate :
             * unitType : 1
             */

            public String endTime;
            public String startTime;
            public String typeDate;
            public int unitType;
        }
    }
}
