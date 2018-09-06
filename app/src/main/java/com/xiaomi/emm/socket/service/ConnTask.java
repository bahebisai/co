package com.xiaomi.emm.socket.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.socket.bean.UserInfo;
import com.xiaomi.emm.socket.bean.UserMgr;
import com.xiaomi.emm.socket.constant.AppConfig;
import com.xiaomi.emm.socket.constant.AppErr;
import com.xiaomi.emm.socket.packet.BasicPacket;
import com.xiaomi.emm.socket.packet.HeartbeatPacket;
import com.xiaomi.emm.socket.packet.MobileRegistePacket;
import com.xiaomi.emm.socket.packet.PacketConstant;
import com.xiaomi.emm.socket.packet.PushDevPacket;
import com.xiaomi.emm.socket.tcp.TcpClient;
import com.xiaomi.emm.socket.threadtaskpool.ResultType;
import com.xiaomi.emm.socket.threadtaskpool.TaskWithResult;
import com.xiaomi.emm.socket.threadtaskpool.ThreadTaskPool;
import com.xiaomi.emm.socket.threadtaskpool.ThreadUtils;
import com.xiaomi.emm.socket.time.StdAlarmImpl;
import com.xiaomi.emm.socket.utils.StringFormatter;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.utils.PreferencesManager;
import com.xiaomi.emm.utils.TheTang;
import com.xiaomi.emm.view.activity.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;



/**
 * 后台长连接服务：
 *
 * 1. 处理长连接心跳（有的手机Alarm通知有问题，需要完善）； 2. 重连 处理自动登录、显式登录； 3. 登录后用户数据的更新 处理服务器下发PUSH消息
 */
public class ConnTask {
	private static final String TAG = "ConnTask";

	private TVBoxService m_service = null;
	private TcpClient    m_client  = null;
	private Bundle bundle;
	// ConnTask是否正在运行：调用了start() - stop()之间为True状态
	private boolean mIsRunning = false;
	private boolean mIsConnecting = false;

	// 接收数据相关
	private ByteBuffer m_recv_buf = ByteBuffer.allocate(32768);
	private byte[] m_byte_buf = new byte[32768];
	private int lenMax=20000;
	private int m_seq = 1000;

	// 用户信息
	private UserInfo m_userinfo;

	// 建立连接时间间隔、连接次数等
	private static int m_connect_interval;
	private int reconnect_count = 0;
	private int reconnect_countMAX = 50;  //5
	private int reconnect_heartbeat_count = 0;
	private boolean receiver_heartbeat_state = false;
	private static final int CONNECT_MAX_INTERVAL = 10 * 1000;
	private static final int CONNECT_MIN_INTERVAL = 2 * 1000;

	// 发送心跳报时间间隔，服务器超时：330秒，发送间隔：240秒
	private static final int HEARTBEAT_INTERVAL = 180000;//15000;//240000

	// 定时发送心跳包时钟ID
	private int m_heartbeat_alarm_id;

	// 发送/接收广播ACTION
	public static String ACTION_CONNTASK_CONNECT = "com.elife.mobile.conntask.connect";
	public static String CONNTASK_CONNECT_MOBILE = "mobile";
	public static String CONNTASK_CONNECT_PWD = "pwd";
	public static String CONNTASK_CONNECT_AUTOLOGIN = "autologin";
	public static String CONNTASK_CONNECT_REMPWD = "rempwd";
	public static String ACTION_CONNTASK_RESULT = "com.elife.mobile.conntask.result";
	public static String CONNTASK_RESULT_TAG = "result";
	public static String CONNTASK_RESULT_MSG = "msg";
	private static final String RECONNECT_ACTION = "reconnect_action";
	private static final String CHECKCONNECT_ACTION = "checkconnect_action";

	//网络波动检查发心跳是否有消息会的状态
	private  boolean sendCheckHeartBeatFlag =false;

	// 广播接收器
	private BroadcastReceiver m_connectReceiver;

	private boolean m_bFromBroadcast;

	// 取设备的电压信号数据
	public static final int UPDATE_SENSOR_DATA = 1008;
	public static int sensor_retry_count = 0;

	// 连接任务对象单例
	private static ConnTask connTask;

	private ThreadTaskPool threadTaskPool;

	PreferencesManager mPreferencesManager = PreferencesManager.getSingleInstance();

	// 系统数据版本
	// private SystemDataVerInfo serVer;
	//private SystemDataVerInfo localVer;

	/**
	 * 获取连接任务对象单例
	 *
	 * @return
	 */
	public static ConnTask getInstance() {
		return connTask;
	}



	/**
	 * 构造方法
	 *
	 * @param service
	 */
	public ConnTask(TVBoxService service) {
		super();
		connTask = this;
		m_service = service;
		StdAlarmImpl.init(m_service);

		// 实例化一个空的用户对象
		m_userinfo = new UserInfo();
		m_bFromBroadcast = false;

		Log.w(TAG, "ConnTask() 连接对象初始化……");
	}

	/**
	 * 开始启动连接任务
	 */
	public synchronized void start() {
		Log.e(TAG,"start-开始启动连接任务");
		// 设置回调接口对象
		m_client = new TcpClient(tcpResponse);
		// 开始建立连接
		connect();

		// 注册广播
		m_connectReceiver = new ConnReqReceiver();
		IntentFilter filter = new IntentFilter();

		filter.addAction(ACTION_CONNTASK_CONNECT);
		filter.addAction(RECONNECT_ACTION);
		filter.addAction(CHECKCONNECT_ACTION);
		if (m_connectReceiver != null) {
			m_service.registerReceiver(m_connectReceiver, filter);
			mIsRunning = true;
		}
	}

	/**
	 * 建立连接的回调对象
	 */
	private TcpClient.ITcpResponse tcpResponse = new TcpClient.ITcpResponse() {
		/**
		 * 成功建立连接
		 */
		@Override
		public void onConnectSucceed() {
			Log.w(TAG,"成功建立连接");
			LogUtil.writeToFile(TAG,"握手成功建立连接长连接服务器");
			mIsConnecting=false;
			// 停止重连时钟(定期尝试建立连接)
			stopReconnectAlarm();

			// 发送登录包
			sendRegisterMsg();

			// 开始发送心跳包
			//startHeartBeatLoop();
		}

		/**
		 * 建立连接失败
		 */
		@Override
		public void onConnectFailed() {
			// 设置是否建立连接标识
			//UserMgr.setBeConn(false);
			// 设置用户是否登录成功
			 Log.w(TAG, "m_response.onConnectFailed --连接失败");
			LogUtil.writeToFile(TAG, "m_response.onConnectFailed --连接失败");
		/*	mIsConnecting=false;
			UserMgr.setLogon(false);
			// 停止重新连接时钟，停止发送心跳包
			stopReconnOpt();*/

			// 尝试重新建立连接，返回本次连接失败提示
			handleConnResult(false, "您的网络异常，请稍后再试吧！");
			Log.w(TAG, "m_response.onConnectFailed --连接失败2");
			LogUtil.writeToFile(TAG, "m_response.onConnectFailed --连接失败2");
		}

		/**
		 * 通过长连接接收信息
		 */
		@Override
		public boolean onRecv(byte[] data, int count) {
			//	Log.w(TAG, "onRecv() 收到数据字节数=" + count);

			//handleRecvMsg(data, count);

			//如果已经发送网络波动发送心跳检测,有消息回来则停止取消网络波动
			if( sendCheckHeartBeatFlag ){
				//初始化标志
				sendCheckHeartBeatFlag =false;

				stopCheckHeartBeat();
			}

			//这里正常心跳检测，如果有消息回来说明心跳正常
			receiver_heartbeat_state = false;
			reconnect_heartbeat_count = 0;

			return handleRecvMsgs( data,  count);
		}
	};


	/**
	 * 处理收到的信息包，处理粘包和半包
	 *
	 * @param bb
	 * @param count
	 */
	private boolean handleRecvMsgs(byte[] bb, int count) {
		Log.w(TAG,"收到count="+count);
		m_recv_buf.put(bb, 0, count);
		boolean onRecv= true;
		while (true) {
			//当前写入缓冲区的位置此时的position只想写入数据的最后以为，len相当数据的长度（length）
			int len = m_recv_buf.position();
			byte[] buf = m_recv_buf.array();

			// 检查协议头长度
			if (len < 10) {
				Log.d(TAG, "接收缓冲数据满10字节数，为： " + len);
				m_recv_buf.clear();
				break;
			}
			// 检查包头第一个字节及包头版本号
			if (buf[0] != (byte) 0xfa && buf[1] != 0x1) {

				byte[] content_byte2 = new byte[count];
				System.arraycopy(buf, 0, content_byte2, 0, count);

				try {
					String content_s = new String(content_byte2, /*CHARSET*/"UTF-8");
					Log.e(TAG, "包头的前两个字节不对: " + buf[0] + "," + buf[1]+" \n"+content_s);

				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();

				}
				m_recv_buf.clear();
				break;
			}

			// 检查包长
			byte[] len_byte = new byte[2];
			System.arraycopy(buf, 7, len_byte, 0, 2);
			int content_len = PacketConstant.net_byte2short(len_byte);
			content_len += 10;
			//这里是收到包头里面的说明的包的大小大于实际缓冲区设置接收的最大值
			if (content_len > 32768) {
				Log.w(TAG, "收到包头里面的说明的包的大小大于实际缓冲区设置接收的最大值： " + content_len);
				//这里需要返回一个相应的消息给长连接

				byte msg_type = (byte) 0x00;
				msg_type = buf[2];
				if (msg_type==PacketConstant.MSG_HEARTBEAT_RESP){   //心跳包
					sendHeartBeatMsg(); //回心跳
				}else if(msg_type == PacketConstant.MSG_PUSH_DEV_REQ){ //数据包
					PushDevPacket push_info = new PushDevPacket();
					// 返回回应包
					push_info.content_str = "the package is to long";
					push_info.content_len = (short) "the package is to long".length();
					byte[] bufs = push_info.build_resp("the package is to long");
					String buff_str = StringFormatter.FormatToString(bufs, 10);
					Log.w(TAG, push_info.toAllString()+"sendNormalResp() 收到消息后发回应包:" + buff_str);

					// 通知长连接收到数据
					m_client.send(ByteBuffer.wrap(buf));
					//tcpResponse.sendResponseData(ByteBuffer.wrap(buf));

				}

				m_recv_buf.clear();
				break;
			}

			//len和count重复
			if (content_len > len) {
				Log.d(TAG, "未满一包： " + content_len + "," + len);
				m_recv_buf.clear();
				onRecv=false;
				break;
			}

			try {

				procOnePacket(buf);

			} catch (Exception e) {
				sendLoginResultBroadcast(false, "登录异常，请稍后再试！");
				m_recv_buf.clear();

				e.printStackTrace();
				Log.e(TAG, "handleRecvMsg() 处理返回包异常=" + TheTang.getExceptionInfo(e));
				break;
			}
			//刚好一包所以清除
			len -= content_len;
			if (len == 0) {
				m_recv_buf.clear();
				break;
			}
			//还有一种可能是收到的包实际上大于包头里面写的包的大小，
			Log.d(TAG, "粘包，后面包长度为 " + len);
			//这里是处理粘包的
			//所以会把位置执行包头里面写的包的位置
			m_recv_buf.position(content_len);
			//从偏移位置(offset)读取/写入一定数量(byteCount)的bytes到指定的byte array(dst)。
			//把缓冲区存的数据先读取到一个数组，
			m_recv_buf.get(m_byte_buf, 0, len);
			//清理缓冲区
			m_recv_buf.clear();
			//再把byte数组存储到缓冲区里
			m_recv_buf.put(m_byte_buf, 0, len);
		}
		return onRecv;
	}



	public synchronized void stop() {
		mIsRunning = false;

		stopReconnOpt();

		if (m_client != null) {
			m_client.close();
		}
		if (m_connectReceiver != null) {
			m_service.unregisterReceiver(m_connectReceiver);
			m_connectReceiver = null;
		}
	}

	/**
	 * 退出登录
	 */
	public static void logout() {
		if (connTask != null) {
			connTask.logoutInner();
		}
	}

	private void logoutInner() {
		//UserMgr.setBeConn(false);
		Log.w(TAG, "logoutInner() setLogon(false)");
		//UserMgr.setLogon(false);
		stopReconnOpt();
	}

	/**
	 * ConnTask是否正在运行， 调用了start()之后，stop()之前为running状态。
	 */
	public synchronized boolean isRunning() {
		return mIsRunning;
	}

	/**
	 * 是否正在连接，条用连接通信后true，只有成功才能回false
	 * @return
	 */
	public synchronized boolean IsConnecting() {
		return mIsConnecting;
	}

	private class ConnReqReceiver extends BroadcastReceiver {
		public void onReceive(Context paramContext, Intent paramIntent) {
			String action = paramIntent.getAction();

			// 登录
			if (action.equals(ACTION_CONNTASK_CONNECT)) {
				// 清除可能存在的获取智能设备的消息
				m_userinfo.mobile = paramIntent.getStringExtra(CONNTASK_CONNECT_MOBILE);
				m_userinfo.password = paramIntent.getStringExtra(CONNTASK_CONNECT_PWD);
				m_userinfo.auto_login = paramIntent.getIntExtra(CONNTASK_CONNECT_AUTOLOGIN, 0);
				m_userinfo.rempsw = paramIntent.getIntExtra(CONNTASK_CONNECT_REMPWD, 0);
				m_bFromBroadcast = true;

				// 停止旧帐号的重连时钟和心跳包
				stopReconnOpt();

				// 如果用户在线， 则发送下线通知， 然后再连接
				if (UserMgr.isLogon()) {
					// 切换用户，需要清除数据
					//	UserMgr.setBeConn(false);
					//	Log.w(TAG, "action.equals(ACTION_CONNTASK_CONNECT) setLogon(false)");
					UserMgr.setLogon(false);
				}

				// 连接
				connect();
			}

			// 重新连接
			if (action.equals(RECONNECT_ACTION)) {
				String packageName = paramIntent.getStringExtra("package");

				if (packageName == null || !packageName.equals(m_service.getPackageName())) {
					return;
				}

				getConnectSvr();
				LogUtil.writeToFile(TAG,"重新连接[- -!]");
				Log.w(TAG, "重新连接[- -!]");
			}


			if( action.equals( CHECKCONNECT_ACTION)){

					if (sendCheckHeartBeatFlag){
						//五秒到，说明没有收到消息，则认为通信失败，TCP链路断了，重连连接
						handleConnResult( false ," 发送检测心跳，过5秒没有收到任何消息" );
					}
			}
		}
	};

	/**
	 * 处理连接失败的结果
	 *
	 * @param result
	 * @param msg
	 */
	private void handleConnResult(boolean result, String msg) {
		if (!result) {
			//连接标志
			mIsConnecting=false;
			UserMgr.setLogon(false);
			// 停止重新连接时钟，停止发送心跳包
			stopReconnOpt();

			Log.e(TAG,"没有网络所以不去连接长连接服务器"+"===="+msg);
			int networkAvaliable = TheTang.getSingleInstance().getNetworkType();
			//Log.e(TAG,"获取当前的网络状态:0：没有网络 1：WIFI网络 2：WAP网络 3：NET网络==="+networkAvaliable);
			if ( TheTang.getSingleInstance().isNetworkConnected()){

				LogUtil.writeToFile(TAG,"有网络通的情况下重新连接长连接服务器"+networkAvaliable +"===="+msg);
				Log.e(TAG,"有网络通的情况下有网络重新连接长连接服务器"+networkAvaliable +"===="+msg);
				reconnect();
			} else {
				LogUtil.writeToFile(TAG,"网络不通所以不去连接长连接服务器"+networkAvaliable+"===="+msg);
				Log.e(TAG,"没有网络所以不去连接长连接服务器"+"===="+msg);

				//如果网络中断，则启动定时
//				if ("true".equals( mPreferencesManager.getComplianceData( Common.lost_compliance ))) {
//					mPreferencesManager.setComplianceData( Common.lost_compliance, System.currentTimeMillis() + "");
//				}
			}

            //连接失败，
			TheTang.getSingleInstance().isLostCompliance(false);

		} else {
			//登陆成功
            TheTang.getSingleInstance().isLostCompliance(true);
		}
	}

	/**
	 * 如果是登录框登录， 则返回结果处理登录框UI
	 *
	 * @param result
	 * @param msg
	 */
	public void sendLoginResultBroadcast(boolean result, String msg) {
		if (m_bFromBroadcast) {
			m_bFromBroadcast = false;

			/*Intent intent = new Intent(ACTION_CONNTASK_RESULT);
			intent.putExtra(CONNTASK_RESULT_TAG, result);
			intent.putExtra(CONNTASK_RESULT_MSG, msg);
			m_service.sendBroadcast(intent);*/
			//String mms="{\"code\":200}";
			String mms="{\"sendId\":\"-2\",\"code\":200}";
			HandlePushPacket.sendBroadcast("Push", "Push", mms);
		}
	}

	/**
	 * 登录失败处理
	 *
	 * @param msg
	 */
	private void loginError(String msg) {
		if (m_bFromBroadcast) {
			m_bFromBroadcast = false;

			Intent intent = new Intent(ACTION_CONNTASK_RESULT);
			intent.putExtra(CONNTASK_RESULT_TAG, false);
			intent.putExtra(CONNTASK_RESULT_MSG, msg);
			m_service.sendBroadcast(intent);
		}
	}

	/**
	 * 发心跳闹钟
	 */
	private void startHeartBeatLoop(  ) {

			m_heartbeat_alarm_id = StdAlarmImpl.scheduleWithFixedDelay(new StdAlarmImpl.IAlarmResponse() {
				@Override
				public void onAlarm() {
					sendHeartBeatMsg();
				}
			}, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL);


	}


	/**
	 * 网络波动情况下发送检测心跳
	 * @param sendCheckHeartBeatFlag
	 */
	public void startCheckHeartBeat(boolean sendCheckHeartBeatFlag){
		this.sendCheckHeartBeatFlag = sendCheckHeartBeatFlag;

		stopCheckHeartBeat();
		//发送心跳
		sendHeartBeatMsg();

		//创建一个5秒的闹钟，等待消息
		Intent intent = new Intent(CHECKCONNECT_ACTION);
		intent.putExtra("package", m_service.getPackageName());
		PendingIntent pendingIntent = PendingIntent.getBroadcast(m_service, 1003, intent,PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmManager = (AlarmManager) m_service.getSystemService(Context.ALARM_SERVICE);

		int ALARM_TYPE = AlarmManager.RTC_WAKEUP;

		// 判断是否是小米、红米手机
		if ("Xiaomi".equalsIgnoreCase(Build.MANUFACTURER) || "HM1SW".equals(Build.MANUFACTURER)) {
			ALARM_TYPE = AlarmManager.RTC;
		}

		alarmManager.set(ALARM_TYPE, System.currentTimeMillis() + 5000, pendingIntent);


	}

	/**
	 * 停止检测发送检测心跳
	 */
	public void stopCheckHeartBeat(){

		//创建一个5秒的闹钟，等待消息
		Intent intent = new Intent(CHECKCONNECT_ACTION);
		intent.putExtra("package", m_service.getPackageName());
		PendingIntent pendingIntent = PendingIntent.getBroadcast(m_service, 1003, intent,PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmManager = (AlarmManager) m_service.getSystemService(Context.ALARM_SERVICE);

		alarmManager.cancel(pendingIntent);

	}

	/**
	 * 停止正常的心跳
	 */
	private void stopHeartBeatLoop() {
		StdAlarmImpl.cancel(m_heartbeat_alarm_id);
	}

	/**
	 * 停止重连的操作， 重连时钟和停止心跳包
	 */
	public void stopReconnOpt() {
		//	Log.w(TAG, "cleanOldMsg");
		UserMgr.setLogon(false);
		//初始化状态
		mIsConnecting =false;
		sendCheckHeartBeatFlag = false;
		receiver_heartbeat_state = false;
		reconnect_heartbeat_count = 0;

		// 停止重连时钟
		stopReconnectAlarm();

		// 清除可能存在的重连和心跳消息
		stopHeartBeatLoop();

		//停止检查发送心跳(防止)
		stopCheckHeartBeat();

		//关闭资源
		if(m_client != null){
			m_client.closeConnProc();

		}
	}

	/**
	 * 获取连接服务器地址, 连接
	 */
	private void connect() {
		m_connect_interval = CONNECT_MIN_INTERVAL;

		getConnectSvr();
	}

	/**
	 * 掉线后重新连接
	 */
	public void reconnect() {
		// 重新连接的时间策略(断连后马上重新连接5次, 5次后还不成功则以2秒的倍数时间重连, 最大时间间隔10秒)
		if (reconnect_count <=reconnect_countMAX /*5*/) {
			m_connect_interval = 0;
		} else {
			m_connect_interval = (reconnect_count - reconnect_countMAX/*5*/) * CONNECT_MIN_INTERVAL;
		}

		// 如果连接5次以后，每10秒连接一次
		if (m_connect_interval > CONNECT_MAX_INTERVAL) {
			m_connect_interval = CONNECT_MAX_INTERVAL;
		}

		Log.w(TAG, "reconnect() 设置时钟，" + m_connect_interval / 1000 + "秒后第" + (reconnect_count + 1)+ "次重新连接。。。");

		AlarmManager alarmManager = (AlarmManager) m_service.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingIntent = getPendingIntent();

		int ALARM_TYPE = AlarmManager.RTC_WAKEUP;

		// 判断是否是小米、红米手机
		if ("Xiaomi".equalsIgnoreCase(Build.MANUFACTURER) || "HM1SW".equals(Build.MANUFACTURER)) {
			ALARM_TYPE = AlarmManager.RTC;
		}

		// 版本大于19时，set不能准确执行，用setExact
		if (Build.VERSION.SDK_INT >= 19) {
			alarmManager.setExact(ALARM_TYPE, System.currentTimeMillis() + m_connect_interval, pendingIntent);
		} else {
			alarmManager.set(ALARM_TYPE, System.currentTimeMillis() + m_connect_interval, pendingIntent);
		}

		reconnect_count++;
	}

	/**
	 * 获取PendingIntent
	 *
	 * @return
	 */
	private PendingIntent getPendingIntent() {
		Intent intent = new Intent(RECONNECT_ACTION);
		intent.putExtra("package", m_service.getPackageName());
		PendingIntent pendingIntent = PendingIntent.getBroadcast(m_service, 1001, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		return pendingIntent;
	}

	/**
	 * 停止重连时钟
	 */
	private void stopReconnectAlarm() {
		// 清除重连时钟（如果有的话）
		AlarmManager am = (AlarmManager) m_service.getSystemService(Context.ALARM_SERVICE);
		am.cancel(getPendingIntent());
	}

	/**
	 * 处理收到的信息包，处理粘包和半包
	 *
	 * @param bb
	 * @param count
	 */
	private void handleRecvMsg(byte[] bb, int count) {
		m_recv_buf.put(bb, 0, count);
		//	Log.w(TAG,"处理收到的信息包，处理粘包和半包情况");
		while (true) {
			int len = m_recv_buf.position();
			byte[] buf = m_recv_buf.array();

			// 检查协议头长度
			if (len < 10) {
				Log.w(TAG, "接收缓冲数据满10字节数，为： " + len);
				LogUtil.writeToFile(TAG, "接收缓冲数据满10字节数，为： " + len);
				break;
			}
			// 检查包头第一个字节及包头版本号
			if (buf[0] != (byte) 0xfa && buf[1] != 0x1) {
				Log.w (TAG, "包头的前两个字节不对: " + buf[0] + "," + buf[1]);
				LogUtil.writeToFile(TAG,"包头的前两个字节不对: " + buf[0] + "," + buf[1]);
				m_recv_buf.clear();
				break;
			}

			// 检查包长
			byte[] len_byte = new byte[2];
			System.arraycopy(buf, 7, len_byte, 0, 2);
			int content_len = PacketConstant.net_byte2short(len_byte);
			content_len += 10;

			if (content_len > count) {
				byte msg_type = (byte) 0x00;
				msg_type = buf[2];

				Log.w(TAG, "发过来的包头里len长度和收到的包长度不一致：  "  +"包头里len="+ content_len+"收到过来总的大小为count="+count);
				LogUtil.writeToFile(TAG,"发过来的包头里len长度和收到的包长度不一致： "  +"包头里len="+ content_len+"收到过来总的大小为count="+count);
				if (msg_type==PacketConstant.MSG_HEARTBEAT_RESP){
					sendHeartBeatMsg();
				}else if(msg_type == PacketConstant.MSG_PUSH_DEV_REQ){

					PushDevPacket push_info = new PushDevPacket();

					// 返回回应包
					push_info.content_str = "the package is to long";
					push_info.content_len = (short) "the package is to long".length();
					byte[] bufs = push_info.build_resp("the package is to long");
					String buff_str = StringFormatter.FormatToString(bufs, 10);
					Log.w(TAG, push_info.toAllString()+"sendNormalResp() 收到消息后发回应包:" + buff_str);
					m_client.send(ByteBuffer.wrap(buf));
				}
				m_recv_buf.clear();
				break;
			}

			if (count < 10) {
				Log.w(TAG, "未满一包： " + content_len + "," + len);
				LogUtil.writeToFile(TAG,"未满一包： " + content_len + "," + len);
				break;
			}

			try {
				procOnePacket(buf);

			} catch (Exception e) {
				//sendLoginResultBroadcast(false, "登录异常，请稍后再试！");
				m_recv_buf.clear();

				e.printStackTrace();
				Log.w(TAG, "handleRecvMsg() 处理返回包异常=" + TheTang.getExceptionInfo(e));
				LogUtil.writeToFile(TAG, "handleRecvMsg() 处理返回包异常=" + TheTang.getExceptionInfo(e));
				break;
			}

			len -= content_len;
			if (len == 0) {
				m_recv_buf.clear();
				break;
			}

			//	Log.w(TAG, "粘包，后面包长度为 " + len);

			m_recv_buf.position(content_len);
			m_recv_buf.get(m_byte_buf, 0, len);
			m_recv_buf.clear();
			m_recv_buf.put(m_byte_buf, 0, len);
		}
	}

	/**
	 * 处理连接中返回的数据包
	 *
	 * @param bb
	 */
	private void procOnePacket(byte[] bb) {
		BasicPacket info = new BasicPacket();
		boolean b_ok = info.setInfo(bb);

		// 是不可用的包
		if (!b_ok) {
			Log.w(TAG, "procOnePacket() not a valid packet ，drop it !!");
			LogUtil.writeToFile(TAG, "procOnePacket() not a valid packet ，drop it !!");
			return;
		}
		//Log.w(TAG,info.toString()+ "procOnePacket()处理连接中返回的数据包"+info.isResponse());
		Log.w(TAG,info.toAllString()+ "----procOnePacket()返回的数据包");
		// 回应包处理   0x80
		if (info.isResponse()) {
			// 是登录回应包  0X82 手机启动应答
			if (info.msg_type == PacketConstant.MSG_UNREGISTE_RESP) {
				// 解析登录包的回应包
				MobileRegistePacket reg_info = new MobileRegistePacket();
				reg_info.setRegPacket(info);

				reconnect_count = 0;
				m_connect_interval = 0;

				// 初始化应用基本信息，以便向服务端发送请求调用
				// APP_ID值
				//m_userinfo.app_id = String.valueOf(AppRuntime.app_id);


				// 登录失败
				if (reg_info.ret != 0) {
					// 登录异常信息
					String errMsg = AppErr.getLoginErrStr(reg_info.ret, "登录异常");

					// 设置登录和连接状态
					//	UserMgr.setBeConn(false);
					Log.w(TAG, "登录失败  reg_info.ret != 0  setLogon(false)");
					UserMgr.setLogon(false);

					// 停止重连时钟，停止发送心跳包时钟
					stopReconnOpt();
					loginError("登录出错 ：" + errMsg);

					Log.w(TAG, "procOnePacket() 登录出错=" + errMsg);
					LogUtil.writeToFile(TAG,"长连接服务器登陆失败===="+errMsg);
					//reconnect();
					// 发送登录包
					//sendRegisterMsg();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					handleConnResult(false,"登陆失败");
					return;
				}

				/*if (TextUtils.isEmpty(m_userinfo.mobile) || TextUtils.isEmpty(m_userinfo.password)) {
					Log.w(TAG, "用户名或密码为空，不处理登录返回包");
					return;
				}*/

				// 上次登录用户
				//	UserInfo lastUser = AppRuntime.getInstance().getLastLoginUser();
				// 有账号登录过，且是切换账号
				//		if (lastUser != null && !m_userinfo.mobile.equals(lastUser.mobile)) {
				//				}
				// 重置Application中的某些属性值
				//	ILog.e(TAG, "AppRuntime.getInstance().clear()");
				//	AppRuntime.getInstance().clear();
				// 是否切换账号
				/*if (lastUser == null || !m_userinfo.mobile.equals(lastUser.mobile)) {
					AppRuntime.isSwitchUser = true;
				} else {
					AppRuntime.isSwitchUser = false;
				}*/
				//AppRuntime.isSwitchUser = true;
				// 服务器返回用户类型，这样通知盒子状态时，用户界面才不会闪一下用户状态为"未登录"
				m_userinfo.user_id = reg_info.user_id;
				m_userinfo.name = reg_info.user_name;
				m_userinfo.user_type = reg_info.user_type;
				m_userinfo.icon_path = reg_info.icon_path;
				/*if (info.msg_type == PacketConstant.MSG_UNREGISTE_RESP) {
					Log.e(TAG,"登录响应"+info.toAllString());
				}*/

				Log.e(TAG,"登录成功响应--"+info.toAllString());
				// 获取通过登录返回的PRODUCT_CODE、PRODUCT_TYPE
				//	WiseDevMgr.setProductCode(reg_info.product_code);
				//	WiseDevMgr.setProductType(reg_info.product_type);

				// 后续使用，所以在这里赋值，要放在(setLogon)方法之前
				//	UserMgr.setUserInfo(m_userinfo);

				// 登录成功后取回的用户信息基本完整，可认为登录完成
				//UserMgr.setSessionId(reg_info.phone_session_id);
				//UserMgr.setBeConn(true);
				UserMgr.setLogon(true);
				m_bFromBroadcast=true;
				handleConnResult(true, "登录成功");
				LogUtil.writeToFile(TAG,"长连接服务器登陆成功");
				/////////////////////////////////////////
				//休眠3秒
				try {
					Thread.sleep( 5000 );
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				// 开始发送心跳包
				startHeartBeatLoop(  );
				/////////////////////////////////////////////

			} else if (info.msg_type == PacketConstant.MSG_HEARTBEAT_RESP) {
				// 心跳回应包
				//	Log.w(TAG, "<<<<<<<<< 收到心跳回应包");
				receiver_heartbeat_state =true;
				Log.w(TAG, ">>>>>>>>>>>>> 收到心跳包=="+info.toAllString());
				LogUtil.writeToFile(TAG,">>>>>>>>>>>>> 收到心跳包=="+info.toAllString());
			}

			return;
		}

		// 是 PUSH包
		if (info.msg_type == PacketConstant.MSG_PUSH_DEV_REQ) {

			PushDevPacket push_info = new PushDevPacket();
			push_info.setPushMobilePacket(info);
            LogUtil.writeToFile(TAG, "push == " + push_info.toString());
			// push 包处理类 , 由 conn_client 操作
			HandlePushPacket handle_push = new HandlePushPacket(m_service);
			handle_push.handlePackage(push_info, m_client);
					/*	Log.w(TAG, "<<<<<<<<< 收到PUSH消息内容=  "+push_info.content_str);

			String msg="{\"ConfigureStrategy\":{\"isWebclip\":1,\"remark\":\"\",\"wifiList\":[{\"macAddress\":\"\",\"ssid\":\"thetang2.4\",\"isHiddenNetwork\":1,\"wifiConfigId\":360,\"securityType\":2,\"configId\":344,\"password\":\"thetang2307\",\"isAutoJoin\":1}],\"issuedNumber\":1,\"adminName\":\"123\",\"lastUpdateTime\":{\"seconds\":44,\"year\":117,\"month\":9,\"hours\":15,\"time\":1509001244000,\"date\":26,\"minutes\":0,\"day\":4,\"timezoneOffset\":-480},\"isAllowWifiConfig\":1,\"adminId\":1,\"vpnList\":[],\"name\":\"lxkwebclip\",\"isWifi\":1,\"webclipList\":[{\"configId\":344,\"webClipName\":\"百度\",\"id\":184,\"webClipImgPath\":\"\\\\fileupload\\\\config_img\\\\10fc40d0-e419-4d30-ab97-ceb26a9a5d91.png\",\"webClipUrl\":\"http:\\/\\/www.baidu.com\"}],\"configId\":344,\"isApn\":0,\"useNumber\":2,\"apnList\":[],\"platformType\":0,\"isVpn\":0},\"code\":147}\n";

			String mms="{\"message\":\"PUSH消息内容\",\"code\":128}";

					/*Intent intent = new Intent(JPushInterface.ACTION_NOTIFICATION_RECEIVED);
					intent.putExtra(JPushInterface.EXTRA_EXTRA,*//*push_info.content_str*//*mms);
					TheTang.getSingleInstance().getContext().sendBroadcast(intent);

*/

		/*

			Intent intentService = new Intent();
			intentService.setClass( TheTang.getSingleInstance().getContext(), MDMOrderExcuteService.class );
			intentService.putExtra( "bundle",bundle);
			TheTang.getSingleInstance().getContext().startService( intentService );
		*/




		}
	}

	/**
	 * 发送心跳包
	 */
	private  void sendHeartBeatMsg() {
		// 只要有连接存在，就发心跳包
		//Log.w(TAG, ">>>>>>>>>>>>> 发送心跳包");
		//心跳包，仅有 10 个字节的包头

		if (receiver_heartbeat_state){
			receiver_heartbeat_state = false;
			reconnect_heartbeat_count = 0;
		}

		//如果两次发送心跳都没有收到，认为通信失败
		if (reconnect_heartbeat_count >= 2){
			handleConnResult(false,"两次发送心跳都没有收到，认为通信失败");
		}


		HeartbeatPacket info = new HeartbeatPacket();
		m_seq++;
		info.build_req(m_seq);
		Log.w(TAG,">>>>>>>>>>>>> 发送心跳包=="+info.toAllString());
		LogUtil.writeToFile(TAG,">>>>>>>>>>>>> 发送心跳包=="+info.toAllString());
		m_client.send(ByteBuffer.wrap(info.buff));
		if (! receiver_heartbeat_state ){
			reconnect_heartbeat_count++ ;
		}


	}

	/**
	 * 发送注册包（登录）
	 */
	private void sendRegisterMsg() {

		m_seq++;

		//	Log.w(TAG, "sendRegisterMsg() mobile = " + m_userinfo.mobile + " , password = "+ m_userinfo.password);

		// 得到本机 IP地址
		InetAddress cli_ip = m_client.getLocalAddress();
		//	String[] str_array_ip = cli_ip.toString().split("/");

		PushDevPacket info = new PushDevPacket();

		//	String buff_str = StringFormatter.FormatToString(info.buff, info.buff.length);
		//String sendData="{\"mac_addr\":\"18123963100\",\"gw_ver\":\"1.1\",\"host_ver\":\"1.1\",\"freq\":\"1\",\"secret_key\":\"aa:bb\",\"short_addr\":\"mm\"}";
		String ms="{\"mac_addr\":\"26\"}";
		JSONObject jsonObject = new JSONObject();
		String alias = mPreferencesManager.getData(Common.alias);
		try {
			jsonObject.put("mac_addr",alias);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		info.content_str=jsonObject.toString();
		info.content_len= (short) jsonObject.toString().length();

		byte[] data = info._packet((byte)0x03, 1, 0, jsonObject.toString());

		Log.w(TAG, "得到本机 IP地址--"+cli_ip+ "发送登陆=="+info.toAllString());
		LogUtil.writeToFile(TAG, "得到本机 IP地址--"+cli_ip+ "发送登陆=="+info.toAllString());
		m_client.send(ByteBuffer.wrap(data));
	}

	/**
	 * 取长连接服务器地址
	 */
	private void getConnectSvr() {
		// Log.w(TAG, "getConnectSvr() 建立连接服务器地址……");
		//连接标志
		mIsConnecting=true;
		new Thread() {
			public void run() {
				ThreadUtils.setToBackground();
				m_recv_buf.clear();

				//PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
				String keepAliveHost = mPreferencesManager.getData(Common.keepAliveHost);
				String keepAlivePort = mPreferencesManager.getData(Common.keepAlivePort);
				boolean whetherLogin = false;
				whetherLogin = mPreferencesManager.getData( Common.token ) != null && mPreferencesManager.getData( Common.alias ) != null;
				if (whetherLogin && (TextUtils.isEmpty(keepAliveHost) || TextUtils.isEmpty( keepAlivePort )) ){
					LogUtil.writeToFile( TAG, "端口=keepAlivePort为空="+keepAlivePort+" 域名=keepAliveHost=为空="+keepAliveHost+ "不去连接 ");
					Log.w( TAG, "端口=keepAlivePort为空="+keepAlivePort+" 域名=keepAliveHost=为空="+keepAliveHost+ "不去连接 ");
					mPreferencesManager.removeData( Common.userName ) ;
					mPreferencesManager.removeData( Common.passWord ) ;
					mPreferencesManager.removeLockPassword( "password" );
					mPreferencesManager.removePassword( "password" );
					TheTang.getSingleInstance().getContext().startActivity(new Intent(TheTang.getSingleInstance().getContext(), LoginActivity.class));
					connTask.stop();
					connTask=null;
					TheTang.getSingleInstance().getContext().stopService(new Intent(TheTang.getSingleInstance().getContext(),TVBoxService.class));
					return;
				}

				String s = GetInetAddress(keepAliveHost);

				Log.e(TAG, "获取到存储本地的端口=="+keepAlivePort+" 域名==="+keepAliveHost );

				if (TextUtils.isEmpty(s)) {
					Log.w(TAG,keepAlivePort+ "域名转化为空"+keepAliveHost);
					LogUtil.writeToFile(TAG,keepAlivePort+ "域名转化为空"+keepAliveHost);
					handleConnResult(false, "域名转化成ip为空="+s);
				} else {
					LogUtil.writeToFile( TAG, "建立连接服务器地址-端口=="+keepAlivePort+" 域名==="+keepAliveHost+"  ,域名转化成ip===" + s +" 开始建立通信……");
					Log.w( TAG, "建立连接服务器地址-端口=="+keepAlivePort+" 域名==="+keepAliveHost+"  ,域名转化成ip===" + s +" 开始建立通信……");
					m_client.connect(s, Integer.valueOf(keepAlivePort));
				}

			}

		}.start();

	}


	public  String GetInetAddress(String host) {
		if (TextUtils.isEmpty( host )){
			return "";
		}

		String IPAddress = "";
		InetAddress ReturnStr1 = null;
		if ( TheTang.getSingleInstance().isNetworkConnected()) {
			try {


				ReturnStr1 = java.net.InetAddress.getByName(host);
				IPAddress = ReturnStr1.getHostAddress();
			} catch (UnknownHostException e) {
				//	e.printStackTrace();
				Log.e(TAG,host+" ,ReturnStr1===null" );
				return IPAddress;
			}
		}
		return IPAddress;
	}

	public 	boolean isIpString(String arg0){



		boolean is=true;
		try {
			InetAddress ia=InetAddress.getByName(arg0);

			Log.e(TAG,arg0+" , InetAddress  ipString="+ia+"  ,ia.getHostAddress()=="+ia.getHostAddress());
		} catch (UnknownHostException e) {
			Log.e(TAG,arg0+" , InetAddress  ipString="+false);
			is=false;
		}
		return is;
	}

	/** 标记登录成功后，数据获取正在进行 */
	private static volatile boolean isLoadingDataOnLogin;

	/**
	 * 登录数据是否正在获取中
	 */
	public static boolean isLoadingDataOnLogin() {
		return isLoadingDataOnLogin;
	}

	/** 标记登录成功后，数据获取正在进行到设备状态查询前 */
	private static volatile boolean isNextQueryDeviceStatus;

	/**
	 * 是否执行到设备状态查询之前
	 * */
	public static boolean isNextQueryDeviceStatus() {
		return isNextQueryDeviceStatus;
	}

	public ThreadTaskPool getThreadTaskPool() {
		return threadTaskPool;
	}

	public synchronized void loadDataOnLogin() {
		isNextQueryDeviceStatus = false;
		isLoadingDataOnLogin = true;

		// 发广播通知数据开始刷新
		Intent intent = new Intent(AppConfig.NOTICE_START_LOADING_DATA_ON_LOGIN);
		TheTang.getSingleInstance().getContext().sendBroadcast(intent);



		threadTaskPool = new ThreadTaskPool(TheTang.getSingleInstance().getContext());
		threadTaskPool.setOnFinishedListener(new ThreadTaskPool.OnFinishedListener() {

			@Override
			public void onFinished() {
				Log.w(TAG, "登录数据加载完成");
				// 发广播通知数据刷新结束
				Intent intent = new Intent(AppConfig.NOTICE_FINISH_LOADING_DATA_ON_LOGIN);
				TheTang.getSingleInstance().getContext().sendBroadcast(intent);
				isLoadingDataOnLogin = false;
			}
		});

		// 1、查询数据版本
		/*threadTaskPool.addTask(QUERY_DATA_VERSION, new TaskWithResult() {
			@Override
			public ResultType run() {
				// 本地数据版本
				CacheDB.local_ver = CacheDB.get_home_ver(home_id);
				// 服务器端数据版本
				BizRet bizRet = CacheDB.getServerHomeVer(home_id);
				if (bizRet.isHttpConnErr()) {
					return ResultType.RET_FAILED;
				} else if (!bizRet.is_ok()) {
					ILog.e(TAG, "获取服务器端数据版本出错：" + bizRet.m_biz_msg + ", code=" + bizRet.m_biz_code);
				} else {
					CacheDB.server_ver = (HomeVerInfo) bizRet.m_biz_object;
				}
				return ResultType.RET_SUCCESS;
			}
		}, null, true);*/

		// 2、加载本地缓存数据
		threadTaskPool.addTask(LOAD_DATA_FROM_LOCAL, new TaskWithResult() {
			@Override
			public ResultType run() {
				// 从本地数据库加载缓存的数据(家庭成员列表、家庭好友列表、非智能设备列表)
				//loadDataFromLocalDB(home_id);
				return ResultType.RET_SUCCESS;
			}
		}, null, false);

		// 3、更新家庭成员数据
		threadTaskPool.addTask(UPDATE_FAMILY_MEMBER, new TaskWithResult() {

			@Override
			public ResultType run() {
				// 每次登陆重新加载家庭成员信息
				boolean loadFamilyMember = true;

				// 是否从云端加载家庭成员信息
				if (loadFamilyMember) {
				/*	// BizRet bizRet = UserMgr.updateHomeInfo(CacheDB.server_ver, CacheDB.local_ver);
					BizRet bizRet = HomeDataMgr.initHomeInfo(CacheDB.server_ver, CacheDB.local_ver);

					if (bizRet.isHttpConnErr()) {
						return ResultType.RET_FAILED;
					} else if (!bizRet.is_ok()) {
						ILog.e(TAG, "从云端加载家庭成员信息失败：" + bizRet.m_biz_msg + ", code="
								+ bizRet.m_biz_code);
					} else {
						CacheDB.del_home_his_by_ver(home_id, CacheDB.HOME_MEMBER);
					}*/
				}
				// 补充本用户信息
				//		UserMgr.replenishUserInfo(m_userinfo, HomeDataMgr.getHomeMemberByMobile(m_userinfo.mobile));
				return ResultType.RET_SUCCESS;
			}
		}, new String[] { QUERY_DATA_VERSION, LOAD_DATA_FROM_LOCAL }, true);






		threadTaskPool.shutdown();
	}

	/** 查询本地和服务器数据版本 */
	private static final String QUERY_DATA_VERSION = "query_data_ver";
	/** 加载本地缓存数据（家庭成员列表、家庭好友列表、非智能设备列表） */
	private static final String LOAD_DATA_FROM_LOCAL = "load_data_from_local";
	/** 更新家庭成员列表信息 */
	private static final String UPDATE_FAMILY_MEMBER = "update_family_member";


}
