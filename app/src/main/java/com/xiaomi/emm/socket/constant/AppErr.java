package com.xiaomi.emm.socket.constant;

import android.text.TextUtils;
import android.util.SparseArray;

import com.xiaomi.emm.socket.common.BizRet;
import com.xiaomi.emm.socket.common.HttpRet;


/**
 * 错误码列表及描述
 */

public class AppErr {

	// 取统一错误字符串
	public static String getErrStr(int err_code) {
		String err_str = err_str_map.get(err_code);
		if (err_str != null) {
			return err_str;
		}

		return "错误码=" + err_code;
	}

	/**
	 * 根据错误码获取错误信息
	 * @param err_code
	 * @return
	 */
	public static String getErrMsg(int err_code, String defaultMsg) {
		String err_msg = err_str_map.get(err_code);

		if(!TextUtils.isEmpty(err_msg)) {
			err_msg = err_msg + "(" + err_code + ")";
		} else {
			err_msg = defaultMsg + "(" + err_code + ")";
		}

		return err_msg;
	}

	/**
	 * 取登录错误字符串
	 * @param err_code
	 * @param defaultMsg
	 * @return
	 */
	public static String getLoginErrStr(int err_code, String defaultMsg) {
		String err_str = err_login_str_map.get(err_code);
		if (err_str != null && !"".equals(err_str)) {
			return err_str;
		}

		return defaultMsg;
	}

	// 取最全错误描述字符串
	public static String getTediousErrStr(int err_code, String old_err_msg) {
		StringBuffer sb = new StringBuffer();
		sb.append("返回码(");
		sb.append(err_code);
		sb.append("),");
		sb.append(getErrStr(err_code));
		if (!TextUtils.isEmpty(old_err_msg)
				&& !"null".equals(old_err_msg)) {
			sb.append(",");
			sb.append(old_err_msg);
		}

		return sb.toString();
	}

	//
	public static String getTediousErrStr(HttpRet http_ret) {
		return getTediousErrStr(http_ret.m_http_code, null);
	}

	//
	public static String getTediousErrStr(BizRet biz_ret) {
		return getTediousErrStr(biz_ret.m_biz_code, biz_ret.m_biz_msg);
	}

	/** 获取业务上的错误码*/
	public static String getBusinessErrStr(int err_code){
		String err_str = err_business_str_map.get(err_code);
		if (err_str != null) {
			return err_str;
		}
		return "执行异常";
	}

	/**
	 * OK码
	 */
	public final static int ERR_OK = 0;
	public final static int OK = 0;

	/**
	 * 中继错误码
	 */
	public final static int ERR_DUMB_UART_SEND_FAILED = 2;
	public final static int ERR_DUMB_INIT_UART_FAILED = 3;
	public final static int ERR_DUMB_SEND_DATA_ILLEGAL = 5;

	public final static int ERR_DUMB_HOPPING_FAILED = 17;
	public final static int ERR_DUMB_TIMEOUT = 18;
	public final static int ERR_DUMB_NO_AVAILABLE_TRANSIT = 19;
	public final static int ERR_DUMB_WAKE_UP_ERROR = 20;
	public final static int ERR_DUMB_HUB_BUSY = 21;
	public final static int ERR_DUMB_HUB_HEAD_ERROR = 22;
	public final static int ERR_DUMB_HUB_LENGTH_ERROR = 23;
	public final static int ERR_DUMB_HUB_SRC_ADDR_ERROR = 24;
	public final static int ERR_DUMB_READ_TEMP_HUMI_IIC = 32;
	public final static int ERR_DUMB_EXECUTE = 34;
	public final static int ERR_DUMB_MAC_ERROR = 35;
	public final static int ERR_DUMB_REQUEST_RETURN_TIMEOUT = 36;

	public final static int ERR_DUMB_CHECKSUM = 48;
	public final static int ERR_DUMB_HALF_PACKET = 49;
	public final static int ERR_DUMB_NO_SAME_INFO = 50;
	public final static int ERR_DUMB_INVALID_CMD = 52;
	public final static int ERR_DUMB_SMART_DUMB_WIFI_ERROR = 53;
	public final static int ERR_DUMB_ACK_ERROR = 54;
	public final static int ERR_DUMB_ACK_LENGTH_ERROR = 55;

	public final static int ERR_DUMB_NEW_FREQUENCY_ERROR = 65;

	// LUA脚本文件不存在
	public final static int ERR_LUA_FILE_NULL = 58;
	//  LUA脚本文件执行出错
	public final static int ERR_LUA_EXEC_ERROR = 57;
	// LUA 参数错误
	public final static int ERR_LUA_PARAM_ERROR = 59;

	/**
	 * 中控错误码
	 */
	public final static int ERR_UNKNOWN = 99;

	// 包格式错
	public final static int ERR_INVALID_PACKET = 100;
	// 网络通信超时
	public final static int ERR_NET_TIME_OUT = 101;
	// 网络通信错
	public final static int ERR_NET_COMM = 102;

	// 与本地代理服务器 连接出错
	public final static int ERR_LOCAL_TCP_CONN = 103;
	// 与本地代理服务器 通讯出错
	public final static int ERR_LOCAL_TCP_COMM = 104;

	// 与本地代理服务器 通讯，队列 满
	public final static int ERR_LOCAL_TCP_Q_FULL = 106 ;
	// 代理服务器上报时，错误的 IP 地址
	public final static int ERR_INVALID_IP = 108;
	public final static int ERR_NO_NEW_PACKET = 109;

	// 非法的 cmd_type
	public final static int ERR_INVALID_CMD_TYPE = 110;
	// 非法的 cmd
	public final static int ERR_INVALID_CMD = 112;
	// 非法的 cmd_content
	public final static int ERR_INVALID_CMD_CONTENT = 113;
	// 地址错
	public final static int ERR_INVALID_ADDR = 114;
	// channel_id 为空或者 出错
	public final static int ERR_INVALID_CHANNEL_ID = 115;

	// 参数错误
	public final static int ERR_INVALID_PARAMETER = 116;

	// 主设备错
	public final static int ERR_INVALID_HUB = 117;
	// 未知的智能设备
	public final static int ERR_INVALID_DEV = 118;
	// 缺少 手机号
	public final static int ERR_NO_MOBILE = 119;
	// 非法 的 session_id
	public final static int ERR_INVALID_SESSION_ID = 120;
	// 提交服务器出错
	public final static int ERR_POST_SVR = 121;


	// 智能设备不在线
	public final static int ERR_WISE_DEV_OFF_LINE = 123;
	// 主机与智能设备不在同一内网
	public final static int ERR_NOT_SAME_LAN = 124;
	// UDP 通讯出错
	public final static int ERR_UDP_COMM = 125;
	// UDP 包格式错
	public final static int ERR_UDP_PACKET = 126;

	// 系统异常错
	public final static int ERR_SYSTEM = 130;

	// 点播节目出错
	public final static int ERR_VIDEO_PLAY = 131;
	// 视频列表为空
	public final static int ERR_VIDEO_LIST_EMPTY = 132;
	public final static int ERR_VIDEO_PLAY_URL = 133;
	public final static int ERR_VIDEO_INDEX = 134;
	public final static int ERR_VIDEO_TYPE = 135;

	// 点播时，电视没联上或者没有开电
	public final static int ERR_TV_NO_POWER = 136;
	// 切信元错
	public final static int ERR_TV_SWITCH = 137;
	// 通信错
	public final static int ERR_TV_COMM = 138;
	// 不支持的 电视遥控类型
	public final static int ERR_UNSUPPORT_TV_IR = 139;

	// TV 正在 打开 ，
	public final static int ERR_TV_BUSY  = 140;

	// 这类电视不支持 这个操作码
	public final static int ERR_TV_NOT_SUPPORT_CMD = 141;
	// TV 设备不存在
	public final static int ERR_TV_NOT_EXIST = 142;

	// 新设备已经加入
	public final static int ERR_NEW_DEV_ADD_ALREADY = 143;
	// 写临时地址错
	public final static int ERR_NEW_DEV_SAVE_TEMP_ADDR = 144;
	// 新设备不存在
	public final static int ERR_NEW_DEV_NOT_EXIST = 145;
	// 配置 新设备 地址出错
	public final static int ERR_NEW_DEV_SAVE_ADDR = 146;
	// 请求 MAC 地址出错
	public final static int ERR_NEW_DEV_REQ_MAC = 147;
	// 配置 wifi 地址出错
	public final static int ERR_NEW_DEV_CONF_WIFI = 148;
	// 配置 wifi 地址 超时
	public final static int ERR_NEW_DEV_CONF_WIFI_OVER_TIME = 149;

	// scene 更新错
	public final static int ERR_SCENE_UPDATE = 152;
	public final static int ERR_EMPTY_SCENE_ID = 153;
	public final static int ERR_SCENE_NOT_FOUND = 154;
	public final static int ERR_SCENE_NO_ACTION = 155;
	public final static int ERR_SCENE_EXECUTING = 156;

	public final static int ERR_MUSIC_RADIO_NOT_EXIST = 160;
	public final static int ERR_MUSIC_LIST_EMPTY = 161;
	// URL 非法
	public final static int ERR_VALID_PLAY_URL = 162;
	// URL 不能播放
	public final static int ERR_MUSIC_PLAY_URL = 163;

	// 音箱 正在 打开 ，
	public final static int ERR_MUSIC_BUSY  = 164;

	//scene 场景关联的设备不存在
	public final static int ERR_SCENE_DEV_NOT_EXIST = 170;

	// 接收到的数据内容为空
	public final static int ERR_PUSH_CONTENT_EMPTY = 175;



	// 文件不存在
	public final static int ERR_FILE_NOT_EXIST = 180;


	// 空调红外控制文件不存在
	public final static int ERR_AC_IR_NOT_EXIST = 189;

	// 腾讯视频没有安装
	public final static int ERR_QQ_PLAYER_NOT_EXIST = 190;

	/**
	 * 手机端异常
	 */
	// 网络异常错误码
	public final static int ERR_HTTP_RESP_INVALID = 600;
	public final static int ERR_HTTP_REQ_INVALID = 601;
	public final static int ERR_HTTP_RESP_EMPTY = 602;
	public final static int ERR_HTTP_NET_OTHER = 610;
	public final static int ERR_HTTP_TIMEOUT = 611;
	public final static int ERR_HTTP_CONNECT_TIMEOUT = 612;
	public final static int ERR_HTTP_ILLEGAL_URL = 613;
	public final static int ERR_HTTP_IO_ERR = 614;

	// 返回内容为空
	public final static int ERR_CONTENT_EMPTY = 651;
	// 系统异常
	public final static int ERR_SYS_ERR = 652;
	// 参数异常
	public final static int ILLEGAL_PARAM = 653;

	/** 主机HTTP请求连接错误 */
	public final static int ERR_LUA_HTTP_CONN_ERROR = 700;
	/** 主机HTTP请求超时 */
	public final static int ERR_LUA_HTTP_TIMEOUT = 701;
	/** 主机TCP连接错误 */
	public final static int ERR_LUA_TCP_CONN_ERROR = 702;
	/** 主机TCP请求超时 */
	public final static int ERR_LUA_TCP_TIMEOUT = 703;
	/** LUA脚本语法错误 */
	public final static int ERR_LUA_SCRIPT_SYNTAX_ERROR = 704;
	/** LUA脚本不存在 */
	public final static int ERR_LUA_SCRIPT_NOT_EXIST = 705;
	/** 配置文件不存在 */
	public final static int ERR_LUA_CONFIG_FILE_NOT_EXIST = 706;
	/** 配置数据为空 */
	public final static int ERR_LUA_CONFIG_DATA_NULL = 707;
	/** 配置数据出错 */
	public final static int ERR_LUA_CONFIG_DATA_ERROR = 708;
	/** 业务参数错 */
	public final static int ERR_LUA_BIZ_PARAM_ERROR = 709;
	/** 非法的cmd_type */
	public final static int ERR_LUA_INVALID_CMD_TYPE = 710;
	/** 非法的cmd */
	public final static int ERR_LUA_INVALID_CMD = 711;
	/** 非法的cmd_content */
	public final static int ERR_LUA_INVALID_CMD_CONTENT = 712;
	/** 腾讯视频服务器请求超时 */
	public final static int ERR_LUA_TENCENT_VIDEO_SERV_ERROR = 713;
	/** 主机HTTP请求包体过大 */
	public final static int ERR_LUA_HTTP_BODY_TOO_LARGE = 714;
	/** 播放器处在DLNA播放模式 */
	public final static int ERR_LUA_PLAYER_DLNA_MODE = 717;
	/** 播放器处在BLUETOOTH播放模式 */
	public final static int ERR_LUA_PLAYER_BT_MODE = 718;
	/** 播放器处在插线播放模式 */
	public final static int ERR_LUA_PLAYER_LINEIN_MODE = 719;

	/**
	 * PHP服务器与CS接口
	 */
	public final static int ERR_CS_TOO_LONG_MSG_BODY = 2001;
	public final static int ERR_CS_TERMINAL_HANDLE = 2002;
	public final static int ERR_CS_TERMINAL_TIMEOUT = 2003;
	public final static int ERR_CS_QUEUE_BLOCK = 2004;
	public final static int ERR_CS_EXCEPTION = 2005;
	public final static int ERR_CS_SEQUENCE = 2006;
	public final static int ERR_CS_SEND_PHP_MSG = 2007;

	/**
	 * PHP服务器与AS接口
	 */
	public final static int ERR_AS_INVALID_EVENT_ID = 1001;
	public final static int ERR_AS_INVALID_APP_ID = 1002;
	public final static int ERR_AS_INVALID_MSISDN = 1003;
	public final static int ERR_AS_INVALID_SESSION_ID = 1004;
	public final static int ERR_AS_INVALID_IP = 1005;
	public final static int ERR_AS_NOT_CONNECT_MOBILE = 1006;
	public final static int ERR_AS_NOT_LOGIN_BOX = 1007;
	public final static int ERR_AS_UNAVAILABLE_USER = 1008;
	public final static int ERR_AS_UNAVAILABLE_DEV = 1009;
	public final static int ERR_AS_NOT_LOGIN_USER = 1010;
	public final static int ERR_AS_UNAVAILABE_SESSION_ID = 1011;
	public final static int ERR_AS_AUTH_QUEUE_BLOCK = 1012;
	public final static int ERR_AS_TIMEOUT = 1013;
	public final static int ERR_AS_ACCESS_DB = 1014;
	public final static int ERR_AS_EXCEPTION_NGNIX_SERVICE = 1015;
	public final static int ERR_AS_EXCEPTION_NGNIX_SYS = 1016;
	public final static int ERR_AS_EXCEPTION_HTTP_REQ = 1017;
	public final static int ERR_AS_ACCESS_MEM_DB = 1018;
	public final static int ERR_AS_INVALID_AUTH_JSON = 1019;
	public final static int ERR_AS_NO_SERVICE = 1020;
	public final static int ERR_AS_EXCEPTION_SERVICE_CALL = 1021;

	public final static int ERR_AS_INVALID_CHANNEL_ID = 1022;

	public final static int ERR_AS_EXCEPTION_OTHER = 1111;

	/**
	 * 手机/PAD终端和CS接口（长链接，登录接口用）
	 */
	public final static int ERR_CS_INVALID_CMD = 0x1;
	public final static int ERR_CS_UNCONNECTED_TERMINAL = 0x2;
	public final static int ERR_CS_UNAVAILABE_WISE_DEV = 0x3;
	public final static int ERR_CS_WRONG_PWD = 0x4;
	public final static int ERR_CS_NOT_FOUND_USER = 0x5;
	public final static int ERR_CS_ACCESS_DB = 0x0a;
	public final static int ERR_CS_EXCEPTION_AS = 0x0f;
	public final static int ERR_CS_RECV_MSG = 0x10;
	public final static int ERR_CS_TIMEOUT_AS = 0x06;
	public final static int ERR_CS_ACCESS_AS_CS = 0x11;
	public final static int ERR_CS_ACCESS_CS = 0x12;
	public final static int ERR_CS_ACCESS_MEM_DB = 0x13;


	/**手机操作设备，发送指令到设备后，对于具体产品，返回具体业务的错误码*/
	//森风窗帘（产品码1537）
	public final static int ERR_CODE_EXECUTE_BUSINESS_UNKNOWN = 10000;
	public final static int ERR_CODE_EXECUTE_1537_ERROR = 10001;
	public final static int ERR_CODE_EXECUTE_1537_NO_STROKE = 10002;
	public final static int ERR_CODE_EXECUTE_1537_BUSY = 10003;
	public final static int ERR_CODE_EXECUTE_1537_OPPOSITE = 10004;

	// 已经收藏视频
	public final static int ALREADY_FAV_VIDEO = 5800;

	/**
	 * 内部实现
	 */

	private static SparseArray<String> err_str_map = new SparseArray<String>();
	private static SparseArray<String> err_login_str_map = new SparseArray<String>();
	private static SparseArray<String> err_business_str_map = new SparseArray<String>();

	static {
		/**
		 * 统一错误
		 */
		err_str_map.put(ERR_OK, "操作成功");
		// 中继
		err_str_map.put(ERR_DUMB_UART_SEND_FAILED, "主机串口端数据发送失败"); // 2
		err_str_map.put(ERR_DUMB_INIT_UART_FAILED, "主机初始化串口驱动失败"); // 3
		err_str_map.put(ERR_DUMB_SEND_DATA_ILLEGAL, "发送指令的数据参数不合法"); // 5

		err_str_map.put(ERR_DUMB_TIMEOUT, "中继跳频失败"); // 17
		err_str_map.put(ERR_DUMB_TIMEOUT, "通讯失败，请检查设备是否异常或距离太远"); // 18
		err_str_map.put(ERR_DUMB_NO_AVAILABLE_TRANSIT, "通讯失败，请检查设备是否异常或距离太远"); // 19
		err_str_map.put(ERR_DUMB_WAKE_UP_ERROR, "通讯失败，请检查设备是否异常或距离太远"); // 20
		err_str_map.put(ERR_DUMB_HUB_BUSY, "中控正处于发送状态"); // 21
		err_str_map.put(ERR_DUMB_HUB_HEAD_ERROR, "发送指令的头码不合法"); // 22
		err_str_map.put(ERR_DUMB_HUB_LENGTH_ERROR, "发送指令的长度不合法"); // 23
		err_str_map.put(ERR_DUMB_HUB_SRC_ADDR_ERROR, "发送指令源地址不匹配"); // 24

		err_str_map.put(ERR_DUMB_READ_TEMP_HUMI_IIC, "温湿度读取失败 IIC检验"); // 32
		err_str_map.put(ERR_DUMB_EXECUTE, "中继执行指令失败"); // 34
		err_str_map.put(ERR_DUMB_MAC_ERROR, "中继请求MAC地址超时"); // 35
		err_str_map.put(ERR_DUMB_REQUEST_RETURN_TIMEOUT, "操作超时，请重试"); // 36

		err_str_map.put(ERR_DUMB_CHECKSUM, "中控校验指令错误"); // 48
		err_str_map.put(ERR_DUMB_HALF_PACKET, "中继收到组合包不完整"); // 49
		err_str_map.put(ERR_DUMB_NO_SAME_INFO, "新设备收到的类型信息不匹配"); // 50
		err_str_map.put(ERR_DUMB_INVALID_CMD, "指令未定义"); // 52
		err_str_map.put(ERR_DUMB_SMART_DUMB_WIFI_ERROR, "Wi-Fi配置失败"); // 53
		err_str_map.put(ERR_DUMB_ACK_ERROR, "返回包数据格式错乱"); // 54
		err_str_map.put(ERR_DUMB_ACK_LENGTH_ERROR, "返回包长度异常"); // 55
		err_str_map.put(ERR_DUMB_NEW_FREQUENCY_ERROR, "新频点不在指定范围内"); // 65

		err_str_map.put(ERR_LUA_FILE_NULL, "LUA脚本文件不存在");
		err_str_map.put(ERR_LUA_EXEC_ERROR, "LUA脚本文件执行出错");
		err_str_map.put(ERR_LUA_PARAM_ERROR, "LUA脚本文件参数出错");

		// ===== 中控 设备 出错信息列表

		err_str_map.put(ERR_LOCAL_TCP_CONN, "连接2.4G代理服务器出错");
		err_str_map.put(ERR_LOCAL_TCP_COMM, "与2.4G代理 服务器 通讯出错");
		err_str_map.put(ERR_INVALID_PACKET, "包格式错");
		err_str_map.put(ERR_LOCAL_TCP_Q_FULL, "操作太快，请稍后再试");
		err_str_map.put(ERR_INVALID_IP, "代理地址错");
		err_str_map.put(ERR_NET_TIME_OUT, "网络通信超时 ");
		err_str_map.put(ERR_NO_NEW_PACKET, "无新数据包");

		err_str_map.put(ERR_INVALID_CHANNEL_ID, "非法的 channel_id");

		err_str_map.put(ERR_INVALID_CMD_TYPE, "非法的 cmd_type");
		err_str_map.put(ERR_INVALID_CMD, "非法的 cmd");
		err_str_map.put(ERR_INVALID_CMD_CONTENT, "非法的 cmd content");
		err_str_map.put(ERR_INVALID_SESSION_ID, "非法的 session id");
		err_str_map.put(ERR_INVALID_ADDR, "设备地址错");
		err_str_map.put(ERR_INVALID_HUB, "中控设备错");
		err_str_map.put(ERR_INVALID_DEV, "未知设备");
		err_str_map.put(ERR_NO_MOBILE, "缺少手机号");
		err_str_map.put(ERR_SYSTEM, "系统异常错");
		err_str_map.put(ERR_INVALID_PARAMETER, "参数错");
		err_str_map.put(ERR_WISE_DEV_OFF_LINE, "智能设备离线，请检查设备网络是否连接");
		err_str_map.put(ERR_UDP_COMM, "UDP通讯出错");
		err_str_map.put(ERR_UDP_PACKET, "UDP 包格式出错");

		// 视频类
		err_str_map.put(ERR_VIDEO_PLAY, "点播视频节目出错");
		err_str_map.put(ERR_VIDEO_LIST_EMPTY, "视频列表为空 ");
		err_str_map.put(ERR_VIDEO_PLAY_URL, "视频已失效，请选择其他节目");
		err_str_map.put(ERR_VIDEO_INDEX, "剧集索引超出");
		err_str_map.put(ERR_VIDEO_TYPE, "没有这个视频类型");


		err_str_map.put(ERR_TV_NO_POWER, "电视电源关闭或信源线未接好");
		err_str_map.put(ERR_TV_SWITCH, "不支持自动开机服务,请手动操作");

		err_str_map.put(ERR_TV_COMM, "发送电视红外控制指令出错");
		err_str_map.put(ERR_UNSUPPORT_TV_IR, "电视遥控类型不支持");
		err_str_map.put(ERR_TV_BUSY, "电视正在操作中，请稍后再试");
		err_str_map.put(ERR_TV_NOT_EXIST, "电视设备不存在");
		err_str_map.put(ERR_TV_NOT_SUPPORT_CMD, "这台电视不支持这个操作码");

		// 配置 新设备
		err_str_map.put(ERR_NEW_DEV_ADD_ALREADY, "新设备已经添加");
		err_str_map.put(ERR_NEW_DEV_SAVE_TEMP_ADDR, "分配临时地址出错");
		err_str_map.put(ERR_NEW_DEV_SAVE_ADDR, "新设备分配新地址出错");
		err_str_map.put(ERR_NEW_DEV_REQ_MAC, "新设备获取网卡地址出错");
		err_str_map.put(ERR_NEW_DEV_CONF_WIFI, "Wi-Fi配置失败");
		err_str_map.put(ERR_NEW_DEV_CONF_WIFI_OVER_TIME, "Wi-Fi配置失败，请检查名称密码并确保该Wi-Fi可被搜索到 ");
		err_str_map.put(ERR_NEW_DEV_NOT_EXIST, "没有找到新设备");

		// 场景类
		err_str_map.put(ERR_SCENE_UPDATE, "场景更新失败，请重试");
		err_str_map.put(ERR_EMPTY_SCENE_ID, "scene ID 为空 ");
		err_str_map.put(ERR_SCENE_NOT_FOUND, "场景已被删除，无法执行");
		err_str_map.put(ERR_SCENE_NO_ACTION, "场景动作列表为空，请编辑动作");
		err_str_map.put(ERR_SCENE_EXECUTING, "场景正在执行，请稍后再试");

		// 音箱类设备
		err_str_map.put(ERR_MUSIC_RADIO_NOT_EXIST, "音乐频道不存在");
		err_str_map.put(ERR_MUSIC_LIST_EMPTY, "歌曲列表为空");
		err_str_map.put(ERR_VALID_PLAY_URL, "歌曲已失效，换一首听吧");
		err_str_map.put(ERR_MUSIC_PLAY_URL, "歌曲已失效，换一首听吧");

		err_str_map.put(ERR_MUSIC_BUSY, "音箱正在打开中，请稍后再试");
		err_str_map.put(ERR_SCENE_DEV_NOT_EXIST, "场景关联的设备不存在");

		// 接收包格式错误类
		err_str_map.put(ERR_PUSH_CONTENT_EMPTY, "接收到的数据内容为空");

		err_str_map.put(ERR_AC_IR_NOT_EXIST, "空调红外文件不存在");
		err_str_map.put(ERR_QQ_PLAYER_NOT_EXIST, "QQ视频播放器没有安装");

		err_str_map.put(ERR_FILE_NOT_EXIST, "文件不存在");


		// HTTP CODE
		err_str_map.put(201, "正常；紧接 POST 命令。");
		err_str_map.put(202, "正常；已接受用于处理，但处理尚未完成。  ");
		err_str_map.put(203, "正常；部分信息 — 返回的信息只是一部分。  ");
		err_str_map.put(204, "正常；无响应 — 已接收请求，但不存在要回送的信息。  ");
		err_str_map.put(301, "已移动 — 请求的数据具有新的位置且更改是永久的。  ");
		err_str_map.put(302, "已找到 — 请求的数据临时具有不同 URI。  ");
		err_str_map.put(303, "请参阅其它 — 可在另一 URI 下找到对请求的响应，且应使用 GET 方法检索此响应。  ");
		err_str_map.put(304, "未修改 — 未按预期修改文档。  ");
		err_str_map.put(305, "使用代理 — 必须通过位置字段中提供的代理来访问请求的资源。    ");
		err_str_map.put(306, "未使用 — 不再使用；保留此代码以便将来使用。    ");
		err_str_map.put(400, "错误请求 — 请求中有语法问题，或不能满足请求。    ");
		err_str_map.put(401, "未授权 — 未授权客户机访问数据。    ");
		err_str_map.put(402, "需要付款 — 表示计费系统已有效。    ");
		err_str_map.put(403, "禁止 — 即使有授权也不需要访问。    ");
		err_str_map.put(404, "找不到 — 服务器找不到给定的资源；文档不存在。    ");
		err_str_map.put(407, "代理认证请求 — 客户机首先必须使用代理认证自身。    ");
		err_str_map.put(415, "介质类型不受支持 — 服务器拒绝服务请求，因为不支持请求实体的格式。    ");
		err_str_map.put(500, "内部错误 — 因为意外情况，服务器不能完成请求。    ");
		err_str_map.put(501, "未执行 — 服务器不支持请求的工具。    ");
		err_str_map.put(502, "错误网关 — 服务器接收到来自上游服务器的无效响应。    ");
		err_str_map.put(503, "无法获得服务 — 由于临时过载或维护，服务器无法处理请求。  ");

		err_str_map.put(ERR_HTTP_RESP_INVALID, "返回内容包体不合法");
		err_str_map.put(ERR_HTTP_REQ_INVALID, "生成内容请求包体出错");
		err_str_map.put(ERR_HTTP_RESP_EMPTY, "返回内容包体为空");
		err_str_map.put(ERR_HTTP_NET_OTHER, "其它网络错误");
		err_str_map.put(ERR_HTTP_TIMEOUT, "网络操作处理超时");
		err_str_map.put(ERR_HTTP_CONNECT_TIMEOUT, "网络连接超时");
		err_str_map.put(ERR_HTTP_ILLEGAL_URL, "不合法的URL");
		err_str_map.put(ERR_HTTP_IO_ERR, "网络IO错误");

		err_str_map.put(ERR_CONTENT_EMPTY, "返回内容为空");
		err_str_map.put(ERR_SYS_ERR, "系统异常");
		err_str_map.put(ILLEGAL_PARAM, "参数不合法");

		// LUA脚本相关
		err_str_map.put(ERR_LUA_HTTP_CONN_ERROR, "主机网络请求连接异常，请稍后重试");
		err_str_map.put(ERR_LUA_HTTP_TIMEOUT, "主机网络请求超时，请稍后重试");
		err_str_map.put(ERR_LUA_TCP_CONN_ERROR, "主机网络TCP连接异常，请稍后重试");
		err_str_map.put(ERR_LUA_TCP_TIMEOUT, "主机网络请求超时，请稍后重试");
		err_str_map.put(ERR_LUA_SCRIPT_SYNTAX_ERROR, "主机lua脚本语法错误");
		err_str_map.put(ERR_LUA_SCRIPT_NOT_EXIST, "主机lua脚本不存在");
		err_str_map.put(ERR_LUA_CONFIG_FILE_NOT_EXIST, "主机配置文件不存在");
		err_str_map.put(ERR_LUA_CONFIG_DATA_NULL, "主机配置数据为空");
		err_str_map.put(ERR_LUA_CONFIG_DATA_ERROR, "主机配置数据出错");
		err_str_map.put(ERR_LUA_BIZ_PARAM_ERROR, "主机业务参数出错");
		err_str_map.put(ERR_LUA_INVALID_CMD_TYPE, "主机不支持的cmd_type");
		err_str_map.put(ERR_LUA_INVALID_CMD, "主机不支持的cmd");
		err_str_map.put(ERR_LUA_INVALID_CMD_CONTENT, "主机不支持的cmd_content");
		err_str_map.put(ERR_LUA_TENCENT_VIDEO_SERV_ERROR, "连接腾讯视频服务器异常，请稍后重试");
		err_str_map.put(ERR_LUA_HTTP_BODY_TOO_LARGE, "主机的HTTP请求包体过大");
		err_str_map.put(ERR_LUA_PLAYER_DLNA_MODE, "播放器处在DLNA播放模式");
		err_str_map.put(ERR_LUA_PLAYER_BT_MODE, "播放器处在蓝牙播放模式");
		err_str_map.put(ERR_LUA_PLAYER_LINEIN_MODE, "播放器处在插线播放模式");

		// PHP服务器与CS接口
		err_str_map.put(ERR_CS_TOO_LONG_MSG_BODY, "Push消息包长度超长");
		err_str_map.put(ERR_CS_TERMINAL_HANDLE, "Push写终端句柄失败");
		err_str_map.put(ERR_CS_TERMINAL_TIMEOUT, "终端超时没有返回push应答");
		err_str_map.put(ERR_CS_QUEUE_BLOCK, "连接服务器push队列堵塞");
		err_str_map.put(ERR_CS_EXCEPTION, "连接服务器异常");
		err_str_map.put(ERR_CS_SEQUENCE, "下发sequence错误");
		err_str_map.put(ERR_CS_SEND_PHP_MSG, "CS发送消息给PHP失败");
		// PHP服务器与AS接口
		err_str_map.put(ERR_AS_INVALID_EVENT_ID, "event_id非法");
		err_str_map.put(ERR_AS_INVALID_APP_ID, "app_id非法");
		err_str_map.put(ERR_AS_INVALID_MSISDN, "手机号码格式错误");
		err_str_map.put(ERR_AS_INVALID_SESSION_ID, "session_id非法");
		err_str_map.put(ERR_AS_INVALID_IP, "IP地址格式错误");
		err_str_map.put(ERR_AS_NOT_CONNECT_MOBILE, "手机没有连接");
		err_str_map.put(ERR_AS_NOT_LOGIN_BOX, "主机离线，请检查主机网络是否连接");
		err_str_map.put(ERR_AS_UNAVAILABLE_USER, "用户不存在，请注册后登录");
		err_str_map.put(ERR_AS_UNAVAILABLE_DEV, "设备不存在");
		err_str_map.put(ERR_AS_NOT_LOGIN_USER, "用户未登录，请登录后重试");
		err_str_map.put(ERR_AS_UNAVAILABE_SESSION_ID, "Session ID不存在");
		err_str_map.put(ERR_AS_AUTH_QUEUE_BLOCK, "鉴权队列堵塞");
		err_str_map.put(ERR_AS_TIMEOUT, "认证服务超时无应答");
		err_str_map.put(ERR_AS_ACCESS_DB, "访问mysql数据异常");
		err_str_map.put(ERR_AS_EXCEPTION_NGNIX_SERVICE, "nginx调用服务异常");
		err_str_map.put(ERR_AS_EXCEPTION_NGNIX_SYS, "nginx系统异常");
		err_str_map.put(ERR_AS_EXCEPTION_HTTP_REQ, "nginx无法读取HTTP消息包");
		err_str_map.put(ERR_AS_ACCESS_MEM_DB, "访问内存数据库异常");
		err_str_map.put(ERR_AS_INVALID_AUTH_JSON, "鉴权消息包非Json包格式");
		err_str_map.put(ERR_AS_NO_SERVICE, "没有服务处理该请求");
		err_str_map.put(ERR_AS_EXCEPTION_SERVICE_CALL, "认证服务器服务调用异常");

		err_str_map.put(ERR_AS_INVALID_CHANNEL_ID, "非法的 channel_id");

		err_str_map.put(ERR_AS_EXCEPTION_OTHER, "系统其它异常");

		// PHP服务
		err_str_map.put(5001, "请求指令未提供");
		err_str_map.put(5002, "请求参数不完整");
		err_str_map.put(5100, "家庭不存在");
		err_str_map.put(5101, "用户不存在");
		err_str_map.put(5102, "用户联系人列表为空");
		err_str_map.put(5103, "已经是家庭好友");
		err_str_map.put(5108, "好友不存在");
		err_str_map.put(5104, "用户已经具备家庭");
		err_str_map.put(5105, "用户密码错误");
		err_str_map.put(5106, "验证码不正确");
		err_str_map.put(5107, "验证码已过期");
		err_str_map.put(5109, "用户已经注册");
		err_str_map.put(5110, "用户没有注册邀请");
		err_str_map.put(5111, "没有可用家庭地址");
		err_str_map.put(5112, "用户类型错误");
		err_str_map.put(5113, "手机号码非法");
		err_str_map.put(5199, "家庭号码资源记录不存在");
		err_str_map.put(5200, "无可用设备地址资源");
		err_str_map.put(5201, "设备不存在");
		err_str_map.put(5202, "设备未分配地址或已经确认");
		err_str_map.put(5203, "设备不存在");
		err_str_map.put(5204, "中控设备未注册");
		err_str_map.put(5205, "设备未预分配");
		err_str_map.put(5206, "设备已经确认");
		err_str_map.put(5207, "设备已经取消");
		err_str_map.put(5450, "健康数据未上报或已经失效");
		err_str_map.put(5451, "数据已失效，已被加入健康档案");
		err_str_map.put(5600, "服务场景不存在");
		err_str_map.put(5601, "服务场景状态异常");
		err_str_map.put(5602, "服务场景已经存在");
		err_str_map.put(5700, "服务不存在");
		err_str_map.put(5701, "用户已经订购该服务");
		err_str_map.put(5702, "订购关系不存在");
		err_str_map.put(5750, "无最近播放音乐");
		err_str_map.put(5751, "音乐电台未定义");
		err_str_map.put(ALREADY_FAV_VIDEO, "视频已经收藏");
		err_str_map.put(5801, "文件超出大小限制");
		err_str_map.put(5802, "文件类型不符");
		err_str_map.put(5803, "文件已经存在");
		err_str_map.put(5804, "文件保存失败");
		err_str_map.put(5805, "无最近播放视频");

		err_str_map.put(5806, "查询视频详情失败");
		err_str_map.put(5807, "视频信息不完整");
		err_str_map.put(5808, "查询播放地址失败");
		err_str_map.put(5809, "视频已失效，请选择其他节目");
		err_str_map.put(5810, "查询筛选信息失败");
		err_str_map.put(5811, "地域分类不存在");
		err_str_map.put(5812, "年份分类不存在");

		err_str_map.put(5997, "数据库操作异常");
		err_str_map.put(5998, "暂时不支持的请求");
		err_str_map.put(5999, "系统异常错误");


		/**
		 * 登录
		 */
		err_login_str_map.put(ERR_OK, "登陆成功");
		err_login_str_map.put(ERR_CS_INVALID_CMD, "请求消息非法");
		err_login_str_map.put(ERR_CS_UNCONNECTED_TERMINAL, "终端未连接");
		err_login_str_map.put(ERR_CS_UNAVAILABE_WISE_DEV, "智能设备信息不存在");
		err_login_str_map.put(ERR_CS_WRONG_PWD, "用户密码错误");
		err_login_str_map.put(ERR_CS_NOT_FOUND_USER, "用户不存在，请注册");
		err_login_str_map.put(ERR_CS_ACCESS_DB, "访问mysql数据库异常");
		err_login_str_map.put(ERR_CS_EXCEPTION_AS, "认证服务器系统异常");
		err_login_str_map.put(ERR_CS_RECV_MSG, "接收消息错误");
		err_login_str_map.put(ERR_CS_TIMEOUT_AS, "认证服务器处理超时");
		err_login_str_map.put(ERR_CS_ACCESS_AS_CS, "连接服务器和认证服务器连接异常");
		err_login_str_map.put(ERR_CS_ACCESS_CS, "连接服务器系统异常");
		err_login_str_map.put(ERR_CS_ACCESS_MEM_DB, "访问内存数据库异常");

		/**处理具体业务*/
		err_business_str_map.put(ERR_CODE_EXECUTE_BUSINESS_UNKNOWN, "未知错误");
		err_business_str_map.put(ERR_CODE_EXECUTE_1537_ERROR, "执行错误");
		err_business_str_map.put(ERR_CODE_EXECUTE_1537_NO_STROKE, "窗帘行程未知，请初始化行程后重试");
		err_business_str_map.put(ERR_CODE_EXECUTE_1537_BUSY, "窗帘正在操作中，请稍后再试");
		err_business_str_map.put(ERR_CODE_EXECUTE_1537_OPPOSITE, "窗帘换向失败，请重试");

	}

	/**
	 * 判断错误码是否是设备错误
	 */
	public static boolean isDeviceErr(int errCode){
		if (errCode > 0 && errCode < 100){
			return true;
		}
		return false;
	}

}
