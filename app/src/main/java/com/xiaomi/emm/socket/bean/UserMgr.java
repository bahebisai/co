package com.xiaomi.emm.socket.bean;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.xiaomi.emm.socket.constant.AppConfig;
import com.xiaomi.emm.socket.service.ConnTask;


/**
 * 用户管理类
 *
 */
public class UserMgr {
	private static final String TAG = "UserMgr";

	private Context m_context = null;

	public static UserMgr getUserMgr() {
		return userMgr;
	}

	// 单例
	private static volatile UserMgr userMgr;
	// 是否接入连接服务器
	private boolean m_bConn = false;

	/**
	 *
	 是否登录成功 ，如果用户设置了默认登录，则连接时，发送用户名、密码进行登录
	 */
	private static boolean m_bLogin = false;
	/**
	* 服务器定期生成的SESSOIN ID
	 */
	private String m_session_id = "";
	// 是否有用户
	private boolean m_bUserAvailable = false;

	private UserInfo m_userinfo;

	// 用户性别: 1 男; 2 女
	public static final int USER_GENDER_MAN = 1;
	public static final int USER_GENDER_WOMAN = 2;

	/**
	 * 构造方法
	 *
	 * @param context
	 */
	private UserMgr(Context context) {
		m_context = context;
		m_userinfo = new UserInfo();
	}

	/**
	 * 获取实例对象
	 *
	 * @param context
	 */
	public static void createInstance(Context context) {
		if (context == null) {
			throw new NullPointerException("UserMgr.createInstance(Context context) context不能为 null.");
		}
		if (userMgr == null) {
			synchronized (UserMgr.class) {
				if (userMgr == null) {
					userMgr = new UserMgr(context);
				}
			}
		}
	}

	/**
	 * 获取用户信息
	 */
	public static UserInfo getUserInfo() {
		return userMgr.m_userinfo;
	}

	/**
	 * 清除用户信息
	 */
	public static void clearUserInfo() {
		userMgr.m_userinfo = new UserInfo();
	}

	/**
	 * 设置用户信息
	 *
	 * @param info
	 */
	public static synchronized void setUserInfo(UserInfo info) {
		Log.w(TAG, "setUserInfo() info=" + (info == null ? "null" : info.toJson().toString()));

		if (info == null) {
			userMgr.m_userinfo = new UserInfo();
		} else {
			userMgr.m_userinfo = info;
		}
		if (TextUtils.isEmpty(userMgr.m_userinfo.mobile)) {
			userMgr.m_bUserAvailable = false;
		} else {
			userMgr.m_bUserAvailable = true;
		}
	}



	/**
	 * 设置用户的上线状态
	 */
	public static void setLogon(boolean isLogin) {
		// 设置登录状态
		userMgr.m_bLogin = isLogin;
		Log.w(TAG,"设置登录状态= "+userMgr.m_bLogin);
		if (isLogin) {
			// 发送上线通知
			Intent intent = new Intent(AppConfig.NOTICE_USER_ONLINE);
		//	userMgr.m_context.sendBroadcast(intent);

		//	Log.w(TAG, "setLogon() 发送广播" + AppConfig.NOTICE_USER_ONLINE);

		} else {
			//发送下线通知
			Intent intent = new Intent(AppConfig.NOTICE_USER_OFFLINE);
		//	userMgr.m_context.sendBroadcast(intent);
		//	Log.w(TAG, "setLogon() 发送广播" + AppConfig.NOTICE_USER_OFFLINE);
		}
	}

	/**
	 * 设置用户是否在线状态
	 *
	 * @param isConn
	 */
	/*public static void setBeConn(boolean isConn) {
		userMgr.m_bConn = isConn;
	}*/

	/**
	 * 是否有用户数据
	 *
	 * @return
	 */
	public static boolean isUserAvailable() {
		return userMgr.m_bUserAvailable;
	}

	/**
	 * 用户是否已经登录
	 *
	 * @return
	 */
	public static boolean isLogon() {
		return m_bLogin;
	}


	/**
	 * 用户是否在线
	 *
	 * @return
	 */
	public static boolean isOnline() {
		return userMgr.m_bConn;
	}

	/**
	 * 返回此次登录后的SESSION_ID
	 *
	 * @return
	 */
	public static String getSessionId() {
		return userMgr.m_session_id;
	}

	/**
	 * 设置登录后的SESSION_ID
	 *
	 * @param session_id
	 */
	public static void setSessionId(String session_id) {
		userMgr.m_session_id = session_id;

		// 登录用户信息中也要存有当前SESSION_ID
		userMgr.m_userinfo.session_id = session_id;
	}





	/**
	 * 发送登录广播
	 *
	 * @param context
	 * @param userInfo
	 */
	public static void sendLoginBroadcast(Context context, UserInfo userInfo) {
		// 检查输入参数
		if (userInfo == null) {
			Log.w(TAG, "sendLoginBroadcast() 发送登录广播，输入用户对象为空");
			return;
		}

		// 发广播以新账号重新连接
		Intent intent = new Intent(ConnTask.ACTION_CONNTASK_CONNECT);
		intent.putExtra(ConnTask.CONNTASK_CONNECT_MOBILE, userInfo.mobile);
		intent.putExtra(ConnTask.CONNTASK_CONNECT_PWD, userInfo.password);
		intent.putExtra(ConnTask.CONNTASK_CONNECT_AUTOLOGIN, userInfo.auto_login);
		intent.putExtra(ConnTask.CONNTASK_CONNECT_REMPWD, userInfo.rempsw);
		context.sendBroadcast(intent);
	}

}
