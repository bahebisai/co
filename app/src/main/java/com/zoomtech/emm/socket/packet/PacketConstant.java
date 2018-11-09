
package com.zoomtech.emm.socket.packet;

public class PacketConstant
{
    // del 移到  NetConfig
    //public static final int PROXY_PORT = 22333 ;
    // 推送消息至手机客户端
    public static final byte MSG_PUSH_MOBILE_REQ = 9;
    public static final byte MSG_PUSH_MOBILE_RESP = (byte)0x89;
    public static final byte MSG_PUSH_DEV_REQ = 9;
    public static final byte MSG_PUSH_DEV_RESP = (byte)0x89;
    // 客户端注册
    public static final byte MSG_MOBILE_REGISTE_REQ = 2;
    public static final byte MSG_MOBILE_REGISTE_RESP = (byte)0x82;

    // 手机发送指令 到 盒子客户端
    public static final byte MSG_MOBILE_CMD_REQ = (byte)0x0a;
    public static final byte MSG_MOBILE_CMD_RESP = (byte)0x8a;

    // 客户端注册
    public static final byte MSG_BOX_REGISTE_REQ = 6;
    public static final byte MSG_BOX_REGISTE_RESP = (byte)0x86;
    // 客户端注销
    public static final byte MSG_UNREGISTE_REQ = 3;
    public static final byte MSG_UNREGISTE_RESP = (byte)0x83;
    // 客户端 重连请求包
    public static final byte MSG_RECONNECT_REQ = 4;
    public static final byte MSG_RECONNECT_RESP = (byte)0x84;
    // 客户端心跳
    public static final byte MSG_HEARTBEAT_REQ = 1;
    public static final byte MSG_HEARTBEAT_RESP = (byte)0x81;

    // 与2.4G 中控设备通信 , socket 的通信 包 及回应包
    public static final byte MSG_SEND_24G_REQ = (byte)0x21;
    public static final byte MSG_SEND_24G_RESP = (byte)0xa1;

    // 与中控设备通信 , 只发送，不需要回应包
    public static final byte MSG_SEND_24G_NO_ACK_REQ = (byte)0x23;
    public static final byte MSG_SEND_24G_NO_ACK_RESP = (byte)0xa3;

    // 与 中控设备通信， 获取主动上报消息包 的   socket 通信 包
    public static final byte MSG_RECV_24G_REQ = (byte)0x22;
    public static final byte MSG_RECV_24G_RESP = (byte)0xa2;

    // 与 2.4G 中控设备通信 , 通用 JSON 命令   socket 通信 包
    public static final byte MSG_SEND_24G_JSON_REQ = (byte)0x24;
    public static final byte MSG_SEND_24G_JSON_RESP = (byte)0xa4;

    // 与 2.4G 中控设备通信 , 获取主动上报 JSON  消息包 的   socket 通信 包
    public static final byte MSG_RECV_24G_JSON_REQ = (byte)0x25;
    public static final byte MSG_RECV_24G_JSON_RESP = (byte)0xa5;

    //中控设备通信 ,  TV 类 通信 包 及回应包
    public static final byte MSG_SEND_TV_REQ = (byte)0x26;
    public static final byte MSG_SEND_TV_RESP = (byte)0xa6;

    // 主机应用与代理通信, 代理层收到硬件上报消息，主动发送的 通信 包 及回应包
    public static final byte MSG_PROXY_RPT_REQ = (byte)0x27;
    public static final byte MSG_PROXY_RPT_RESP = (byte)0xa7;

    // 第三方客户端心跳包
    public static final byte MSG_THIRD_HEARTBEAT_REQ = (byte)0x30;
    public static final byte MSG_THIRD_HEARTBEAT_RESP = (byte)0xB0;

    // 第三方(TCL)请求包
    public static final byte MSG_THIRD_TCL_REQ = (byte)0x31;
    public static final byte MSG_THIRD_TCL_RESP = (byte)0xB1;

    // 30 秒 ， 超时时间
    public static final  int ReceiveTimeout = 10 * 1000 ;

    public static short net_byte2short(byte[] bb ) {
        int i = (int) ( ((bb[0] & 0xff) << 8)
                |  ((bb[1] & 0xff) << 0)  );
        return  (short)i ;
    }
}

