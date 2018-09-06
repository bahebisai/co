package com.xiaomi.emm.socket.service;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.xiaomi.emm.socket.constant.AppErr;
import com.xiaomi.emm.socket.packet.PushDevPacket;
import com.xiaomi.emm.socket.tcp.TcpClient;
import com.xiaomi.emm.socket.threadtaskpool.ThreadUtils;
import com.xiaomi.emm.socket.utils.Formatter;
import com.xiaomi.emm.socket.utils.StringFormatter;
import com.xiaomi.emm.features.excute.MDMOrderReciver1;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.utils.TheTang;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;


/**
 * 手机端长连接接收的 PUSH 消息处理， 如果有必要，在状态栏上增加提示消息 , 提示消息中的图标大小为 48*48 目前有以下几类消息 1、好友分享的消息 2、用户订阅，推送的内容消息 3、好友管理中，增加好友，删除好友类消息
 * 4、OTT盒子上线、下线消息 5、红外触发消息
 */
public class HandlePushPacket {
	private final static String TAG = "HandlePushPacket";

	// 生成的设备消息操作类型（添加，删除，修改）
	public static final int OPT_ADD = 1;
	public static final int OPT_DEL = 2;
	public static final int OPT_MOD = 3;
	public static final int OPT_RESET = 4;


	// 21.查询/设置客户端参数配置·
	public static final String MSG_TYPE_QUERY_APP_CONFIG = "qry";


	private TcpClient tcpClient;
	// 推送给 智能终端的消息包
	private PushDevPacket pushDataPacket;

	/** 推送消息处理监听接口 */
	public static interface PushMsgHandleListener {
		/** 接收消息 */
		public void onReceive(JSONObject jsonMsg) throws Exception;
	}

	/** 消息处理监听接口集合 */
	private static Map<String, PushMsgHandleListener> msgHandleListenerMap;

	/**
	 * 构造方法
	 *
	 * @param context
	 */
	public HandlePushPacket(Context context) {

	}

	/**
	 * 获取Push包的长度
	 *
	 * @param content
	 * @return
	 */
	private int getPackgeSize(String content) {
		int length = 0;
		// 检查输入
		if (TextUtils.isEmpty(content)) {
			return length;
		}
		// 获取包的长度
		try {
			length = (content.getBytes("utf-8")).length;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return length;
	}

	/**
	 * 处理收到的Push消息
	 *
	 * @param packageData
	 * @param tcp_client
	 */
	public void handlePackage(final PushDevPacket packageData, TcpClient tcp_client) {
		tcpClient = tcp_client;
		pushDataPacket = packageData;
		PushDevPacket pushDevPackets = new PushDevPacket();
		pushDevPackets.setPushMobilePacket(pushDataPacket);
		// 返回回应包
		new Thread(new Runnable() {
			@Override
			public void run() {
				sendNormalResp(/*packageData*/pushDevPackets);
			}
		}).start();
		// 检查当前用户是否已经登录
		/*if (!UserMgr.isLogon()) {
			//  Log.w(TAG, "handlePackage() 处理收到的PUSH消息, 当前用户没有登录");
			sendNormalResp(packageData);
			return;
		}*/

/*		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String cmd_type= Formatter.byte2str(packageData.msg_type);
					Log.w(TAG, "handlePackage() 接收到的PUSH数据包cmd_type=" + cmd_type);
					// CMD_TYPE为空
					if (TextUtils.isEmpty(cmd_type)) {
						Log.w(TAG, "handlePackage() 收到PUSH消息，类型[cmd_type]为空");
						return;
					}
					if (!TextUtils.isEmpty(packageData.content_str)){
						String msg="{\"ConfigureStrategy\":{\"isWebclip\":1,\"remark\":\"\",\"wifiList\":[{\"macAddress\":\"\",\"ssid\":\"thetang2.4\",\"isHiddenNetwork\":1,\"wifiConfigId\":360,\"securityType\":2,\"configId\":344,\"password\":\"thetang2307\",\"isAutoJoin\":1}],\"issuedNumber\":1,\"adminName\":\"123\",\"lastUpdateTime\":{\"seconds\":44,\"year\":117,\"month\":9,\"hours\":15,\"time\":1509001244000,\"date\":26,\"minutes\":0,\"day\":4,\"timezoneOffset\":-480},\"isAllowWifiConfig\":1,\"adminId\":1,\"vpnList\":[],\"name\":\"lxkwebclip\",\"isWifi\":1,\"webclipList\":[{\"configId\":344,\"webClipName\":\"百度\",\"id\":184,\"webClipImgPath\":\"\\\\fileupload\\\\config_img\\\\10fc40d0-e419-4d30-ab97-ceb26a9a5d91.png\",\"webClipUrl\":\"http:\\/\\/www.baidu.com\"}],\"configId\":344,\"isApn\":0,\"useNumber\":2,\"apnList\":[],\"platformType\":0,\"isVpn\":0},\"code\":147}\n";
						String mms="{\"message\":\"PUSH消息内容\",\"code\":128}";
						LogUtil.writeToFile(TAG,"长连接收到的命令=="+packageData.content_str);
						Log.w(TAG,"长连接收到的命令=="+packageData.content_str);
						sendBroadcast("Push", "Push"*//*JPushInterface.EXTRA_EXTRA*//*,*//*mms*//*packageData.content_str);
					}
					Log.w(TAG, "handlePackage() ---> thread=" + Thread.currentThread().getName());
				} catch (Exception e) {
					Log.w(TAG, e);
				}
			}
		}).start();*/

		//baii
		//TheTang.getSingleInstance().getThreadPoolObject().submit
        //.start()
		new Thread( new Runnable() {
			@Override
			public void run() {

			//	ThreadUtils.setToBackground();

				JSONObject json = null;
				try {
					//Thread.sleep(3000);
					// 获取PUSH包内容
				//	Log.w(TAG, "handlePackage() 接收到的PUSH数据包content=" + packageData.content_str);
//					json = new JSONObject(packageData.content_str);
					/*if (json.has("json_data")){
						// BASE64解密包内容
						String decode_content = BASE64.getFromBASE64(json.getString("json_data"));
						Log.w(TAG, "handlePackage() 接收到的PUSH数据包decode_content=" + decode_content);
						json = new JSONObject(decode_content);
					} else {
					//	Log.w(TAG, "handlePackage() 接收到的PUSH数据包content=" + packageData.content_str);
					}*/

					// 消息命令类型
				//	String cmd_type = json.getString("cmd_type");

					String cmd_type= Formatter.byte2str(packageData.msg_type);
					Log.w(TAG, "handlePackage() 接收到的PUSH数据包cmd_type=" + cmd_type);
					// CMD_TYPE为空
					if (TextUtils.isEmpty(cmd_type)) {
						Log.w(TAG, "handlePackage() 收到PUSH消息，类型[cmd_type]为空");
						return;
					}
					if (!TextUtils.isEmpty(packageData.content_str)){

						//发广播
					/*Intent intent = new Intent(JPushInterface.ACTION_NOTIFICATION_RECEIVED);
					intent.putExtra(JPushInterface.EXTRA_EXTRA,*//*push_info.content_str*//*mms);
					TheTang.getSingleInstance().getContext().sendBroadcast(intent);
					*/
						String msg="{\"ConfigureStrategy\":{\"isWebclip\":1,\"remark\":\"\",\"wifiList\":[{\"macAddress\":\"\",\"ssid\":\"thetang2.4\",\"isHiddenNetwork\":1,\"wifiConfigId\":360,\"securityType\":2,\"configId\":344,\"password\":\"thetang2307\",\"isAutoJoin\":1}],\"issuedNumber\":1,\"adminName\":\"123\",\"lastUpdateTime\":{\"seconds\":44,\"year\":117,\"month\":9,\"hours\":15,\"time\":1509001244000,\"date\":26,\"minutes\":0,\"day\":4,\"timezoneOffset\":-480},\"isAllowWifiConfig\":1,\"adminId\":1,\"vpnList\":[],\"name\":\"lxkwebclip\",\"isWifi\":1,\"webclipList\":[{\"configId\":344,\"webClipName\":\"百度\",\"id\":184,\"webClipImgPath\":\"\\\\fileupload\\\\config_img\\\\10fc40d0-e419-4d30-ab97-ceb26a9a5d91.png\",\"webClipUrl\":\"http:\\/\\/www.baidu.com\"}],\"configId\":344,\"isApn\":0,\"useNumber\":2,\"apnList\":[],\"platformType\":0,\"isVpn\":0},\"code\":147}\n";

						String mms="{\"message\":\"PUSH消息内容\",\"code\":128}";
						//sendBroadcast(JPushInterface.ACTION_NOTIFICATION_RECEIVED, JPushInterface.EXTRA_EXTRA,/*mms*/packageData.content_str);
						//sendBroadcast("Push", "Push",/*mms*/packageData.content_str);
						LogUtil.writeToFile(TAG,"长连接收到的命令=="+packageData.content_str);
						Log.w(TAG,"长连接收到的命令=="+packageData.content_str);
						sendBroadcast("Push", "Push"/*JPushInterface.EXTRA_EXTRA*/,/*mms*/packageData.content_str);
					}
					// 注册推送查询手机客户端配置信息消息监听
					/*if (cmd_type.equals(MSG_TYPE_QUERY_APP_CONFIG)) {
						registerHandler();
					}*/

					Log.w(TAG, "handlePackage() ---> thread=" + Thread.currentThread().getName());

					// 调用相应的处理器处理该消息
					//	excuteMsgHandler(cmd_type, json);
				} catch (Exception e) {
					Log.w(TAG, e);
				}
			}
		}).start();
	}

	/**
	 * 写入普通回应包
	 *
	 * @param push_info
	 */
	private void sendNormalResp(PushDevPacket push_info) {
		//push_info.content_str = /*null*/"sucess";
		//push_info.content_len = /*0*/(short) "sucess".length();

		byte[] buf = push_info.build_resp(push_info.seq, "success");
		String buff_str = StringFormatter.FormatToString(buf, 10);
		Log.w(TAG, push_info.toAllString()+"sendNormalResp() 收到消息后发回应包:" + buff_str);
		tcpClient.send(ByteBuffer.wrap(buf));


	}

	public void registerHandler() {
		// 信息查询及参数配置命令
		HandlePushPacket.registerMsgHandler(HandlePushPacket.MSG_TYPE_QUERY_APP_CONFIG,
				new PushMsgHandleListener() {
					@Override
					public void onReceive(final JSONObject jsonMsg) throws Exception {
						qry(jsonMsg);
					}
				});
	}

	/**
	 * 信息查询及参数配置命令
	 *
	 * @param json_msg
	 * @throws Exception
	 */
	private void qry(JSONObject json_msg) throws Exception {
		final String m_cmd = json_msg.getString("cmd");
		final String m_cmd_content = json_msg.getString("cmd_content");

		try {
			// 用户动态信息， 返回 json 格式 内容
			if ("user_info".equals(m_cmd)) {
				Log.w(TAG,m_cmd+"  ,用户动态信息， 返回 json 格式 内容");
				/*JSONObject json = QryControl.get_user_info(AppRuntime.getInstance());
				String str = json.toString();
				sendRetPacket(AppErr.OK, str.toString());*/
			}

			// 对全局变量参数进行设置
			else if ("set_conf".equals(m_cmd)) {
				JSONObject json = new JSONObject(m_cmd_content);
				Log.w(TAG,m_cmd+"  ,对全局变量参数进行设置"+json.toString());
				// int conf_ret = QryControl.set_conf(json);
			//	sendRetPacket(conf_ret, "");
			}

			// 对全局变量参数进行 查询
			else if ("get_conf".equals(m_cmd)) {
			//	JSONObject json = QryControl.get_conf();
			//	sendResponsePacket(json);

			} else {
				sendRetPacket(AppErr.ERR_INVALID_CMD, "unsupport cmd");
			}

		} catch (Exception e) {
			sendRetPacket(AppErr.ERR_SYSTEM, "程序异常错");

			e.printStackTrace();
			Log.w(TAG, "qry() OMS查询客户端信息及参数配置异常=" + TheTang.getExceptionInfo(e));
		//	Log.w(TAG, e);
		}
	}

	/**
	 * 发送结果回应包
	 *
	 * @param msg_id
	 * @param msg
	 */
	private void sendRetPacket(int msg_id, String msg) {
		try {
			JSONObject json_ret = new JSONObject();
			json_ret.put("ret", msg_id);
			json_ret.put("msg", msg);

			String json_str = json_ret.toString();
		//	String encode_str = BASE64.getBASE64(json_str);
			// 打包成一个 json 格式
			JSONObject json_base64 = new JSONObject();
			json_base64.put("json_data", /*encode_str*/json_str);

			// TCP 方式 发送数据包
			byte[] buf = pushDataPacket.build_resp(json_base64.toString());
			tcpClient.send(ByteBuffer.wrap(buf));

		} catch (Exception e) {
			e.printStackTrace();
			Log.w(TAG, "send_packet() 发送OMS查询客户端信息的回应包异常=" + TheTang.getExceptionInfo(e));
		}
	}

	/**
	 * 根据结构，构造发送回应包
	 *
	 * @param ret_json
	 */
	private void sendResponsePacket(JSONObject ret_json) {
		Log.w(TAG, "sendResponsePacket() OMS查询客户端全局变量的回应包数据=" + ret_json);

		try {
			String json_str = ret_json.toString();
			//String encode_str = BASE64.getBASE64(json_str);

			// 打包成一个 json 格式
			//JSONObject json_base64 = new JSONObject();
		//	json_base64.put("json_data", encode_str);

			// TCP 方式 发送数据包
			byte[] buf = pushDataPacket.build_resp(/*json_base64.toString()*/json_str);
			tcpClient.send(ByteBuffer.wrap(buf));

		} catch (Exception e) {
			e.printStackTrace();
			Log.w(TAG, "sendResponsePacket() 发送OMS查询客户端全局变量的回应包异常=" + TheTang.getExceptionInfo(e));
		}
	}

	/**
	 * 注册消息处理监听器
	 *
	 * @param cmdType
	 * @param handler
	 */
	private static void registerMsgHandler(String cmdType, PushMsgHandleListener handler) {
		if (TextUtils.isEmpty(cmdType)) {
			Log.w(TAG, "registerMsgHandler() 参数cmdType为空");
			return;
		}
		if (handler == null) {
			Log.w(TAG, "registerMsgHandler() 参数hadler为空");
			return;
		}

		// 处理者监听器集合为空
		if (msgHandleListenerMap == null) {
			msgHandleListenerMap = new HashMap<String, PushMsgHandleListener>();
		}

		// 加入集合
		msgHandleListenerMap.put(cmdType, handler);
	}

	/**
	 * 注销消息处理监听器
	 *
	 * @param cmdType
	 */
	public static void unregisterMsgHandler(String cmdType) {
		if (TextUtils.isEmpty(cmdType)) {
			Log.w(TAG, "unregisterMsgHandler() 参数cmdType为空");
			return;
		}

		if (msgHandleListenerMap == null || !msgHandleListenerMap.containsKey(cmdType)) {
			Log.w(TAG, "unregisterMsgHandler() 监听器集合为空或者没有对应消息类型的监听器");
			return;
		}

		msgHandleListenerMap.remove(cmdType);
	}

	/**
	 * 执行对应消息的监听器
	 *
	 * @param cmdType
	 * @param jsonMsg
	 * @return
	 */
	public static boolean excuteMsgHandler(String cmdType, JSONObject jsonMsg) throws Exception {
		// 检查输入参数
		if (TextUtils.isEmpty(cmdType)) {
			Log.w(TAG, "excuteMsgHandler() 参数cmdType为空");
			return false;
		}
		if (jsonMsg == null) {
			Log.w(TAG, "excuteMsgHandler() 参数jsonMsg为空");
			return false;
		}

		// 监听器集合为空
		if (msgHandleListenerMap == null) {
			Log.w(TAG, "excuteMsgHandler() 消息处理监听器集合为空。");
			return false;
		}

		// 没有对应的监听器
		if (!msgHandleListenerMap.containsKey(cmdType) || msgHandleListenerMap.get(cmdType) == null) {
			Log.w(TAG, "excuteMsgHandler() 集合中没有该类型消息处理监听器cmdType=" + cmdType);
			return false;
		}

		// 调用监听器
		PushMsgHandleListener listener = msgHandleListenerMap.get(cmdType);
		listener.onReceive(jsonMsg);
		return true;
	}

	/**
	 * 发送广播
	 *
	 * @param action
	 */
	public   /*synchronized */static void sendBroadcast(String action,String key,String value) {

		// 发送广播
		/*Intent intent = new Intent(action);
		if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
			intent.putExtra(key,value);
		}*/
		//TheTang.getSingleInstance().getContext().sendBroadcast(intent);
		try {
			Bundle bundle = new Bundle(  );
			bundle.putString( key, value );
			MDMOrderReciver1.getSingleInstance().handleMessage( bundle );

		}catch (Exception e){
			e.printStackTrace();
		}

	}



}
