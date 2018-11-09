package com.zoomtech.emm.features.policy.sms;

public class SmsBackupInfo {
/*    　　_id：          短信序号，如100
　　thread_id：对话的序号，如100，与同一个手机号互发的短信，其序号是相同的
　　address：  发件人地址，即手机号，如+86138138000
            　　person：   发件人，如果发件人在通讯录中则为具体姓名，陌生人为null
　　date：       日期，long型，如1346988516，可以对日期显示格式进行设置
　　protocol： 协议0SMS_RPOTO短信，1MMS_PROTO彩信
　　read：      是否阅读0未读，1已读
　　status：    短信状态-1接收，0complete,64pending,128failed
　　type：       短信类型1是接收到的，2是已发出
　　body：      短信具体内容
　　service_center：短信服务中心号码编号，如+8613800755500*/

    private String mAddress;
    private String mPerson;
    private String mDate;
    private int mRead;
    private String mType;
    private String mBody;

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

    public int getRead() {
        return mRead;
    }

    public void setRead(int read) {
        this.mRead = read;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        this.mType = type;
    }

    public String getBody() {
        return mBody;
    }

    public void setBody(String body) {
        this.mBody = body;
    }

}
