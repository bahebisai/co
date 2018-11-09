package com.zoomtech.emm.socket.constant;


/**
 * 应用的配置项
 *
 */
public class AppConfig {

	/**
	 * 初始化
	 */
	public static void init() {
		/*ILog.setLogEnable(LogType.LOGCAT, AppPreferences.getBoolean("log_cat", true));
		ILog.setLogEnable(LogType.FILE, AppPreferences.getBoolean("log_file", true));
		ILog.setLogEnable(LogType.NETWORK, AppPreferences.getBoolean("log_report", false));
		b_force_php = AppPreferences.getBoolean("force_php", true);*/
	}







	/** GPS位置上报 */
	public static boolean b_gps_report = true;
	public static int g_gps_interval = 1800 * 1000;

	/** 升级检查间隔 */
	public static int g_update_interval = 12 * 3600 * 1000;

	/** 广播通知定义 */
	/** 主界面开始刷新数据 */
	public static final String NOTICE_START_LOADING_DATA_ON_LOGIN = "com.elife.notice.start_loading_data_on_login";
	/** 主界面刷新数据结束 */
	public static final String NOTICE_FINISH_LOADING_DATA_ON_LOGIN = "com.elife.notice.finish_loading_data_on_login";
	/** 主界面刷新数据到设备状态之前，可以显示设备列表 */
	public static final String NOTICE_FINISH_LOADING_DATA_BEFORE_DEVICE_STATUS = "com.elife.notice.finish_loading_data_before_device_status";

	// 用户在线通知
	public static final String NOTICE_USER_ONLINE = "com.elife.notice.user_online";
	/** 用户登录后，用户信息补全完毕的通知 */
	public static final String NOTICE_USER_INFO_REPLENISH = "com.elife.notice.user_info_replenish";
	/** 家庭信息修改后，通知其他界面更新 */
	public static final String NOTICE_HOME_INFO_MODIFIED = "com.elife.notice.home_info_modified";
	/** 更新本地智能设备完成 */
	public static final String NOTICE_UPDATE_WISE_DEVICE = "com.elife.notice.update_wise_device";
	// 用户离线通知
	public static final String NOTICE_USER_OFFLINE = "com.elife.notice.user_offline";
	// 主机在线通知
	public static final String NOTICE_BOX_ONLINE = "com.elife.notice.box_online";
	// 主机离线通知
	public static final String NOTICE_BOX_OFFLINE = "com.elife.notice.box_offline";
	/** 登录后查询智能设备列表完成 */
	public static final String NOTICE_LOGIN_WISE_DEV_LOADED = "com.elife.notice.login_wise_dev_loaded";
	/** 登录后查询非智能设备列表完成 */
	public static final String NOTICE_LOGIN_DUMB_DEV_LOADED = "com.elife.notice.login_dumb_dev_loaded";
	/** 登录后查询所有设备信息完成 */
	public static final String NOTICE_LOGIN_DEV_INFO_LOADED = "com.elife.notice.login_all_dev_info_loaded";

	// PUSH消息中的状态变更通知
	public static final String STATUS_CHANGE = "com.elife.status.change";
	// PUSH消息中的数据变更通知
	public static final String DATA_CHANGE = "com.elife.data.change";



}
