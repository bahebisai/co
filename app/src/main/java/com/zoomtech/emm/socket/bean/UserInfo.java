package com.zoomtech.emm.socket.bean;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;


/**
 * 用户信息 类
 * @param <>
 */
public class UserInfo implements Serializable {
	private static final long serialVersionUID = -8809571016358664006L;
	private static final String TAG = "com.elife.sdk.model.user.UserInfo";

	// 用户类型：1 管理员；2普通成员
	public static final String USER_TYPE_MANAGER = "1";
	public static final String USER_TYPE_NORMAL = "2";
	// 表名称
	public static final String TABLENAME = "userinfo";

	/** 用户资料信息 */
	// 用户ID（暂时没用，置为手机号码）
	public String user_id;
	// 用户名称
	public String name;
	// 用户头像
	public String icon_path;
	// 用户签名
	public String sign;
	// 用户手机号码
	public String mobile;
	// 用户类型（1管理用户2普通用户）
	public String user_type;
	// 用户邮箱
	public String email;
	// 用户性别
	public int gender;
	// 用户身高
	public int height;
	// 用户年龄范围 （输入具体的年龄，用户可能会抵触）
	public int age_range;
	// 用户年龄
	public int age;
	public int sport_level;



	/** 登录相关 */
	// 用户密码
	public String password;
	// 最后一次登录时间
	public String login_time;
	// 是否保存密码
	public int rempsw;
	// 是否自动登录
	public int auto_login = 1;

	/** 辅助 */
	public String verf_code; // 注册校验码

	/** 可去掉 */
	// 共享标志
	public int flag_share;
	public String status; // 用户状态：0 未激活、1 已激活未注册、 2 注册3 已注销
	public String is_device; // 是否有相关设备





	/** 基础信息 */
	// SESSION_ID
	public String session_id;
	// APP_ID
	public String app_id;
	// channel_id
	public String channel_id;

	/** 密钥，摄像头管理相关 */
	public String secretKey;


	/**
	 * 返回创建用户数据表的SQL语句
	 *
	 * @return String
	 */
	public static String makeUserCreateTableSQL() {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE ");
		sb.append("user_info");
		sb.append(" (");
		// 个人基本信息
		sb.append("ID TEXT ,");
		sb.append("NAME TEXT,");
		sb.append("ICON TEXT,");
		sb.append("SIGN TEXT,");
		sb.append("MOBILE TEXT,");
		sb.append("TYPE TEXT,");
		sb.append("GENDER INTEGER,");
		sb.append("HEIGHT INTEGER,");
		sb.append("AGE_RANGE INTEGER,");
		sb.append("SPORT_LEVEL INTEGER,");
		// 家庭信息
		sb.append("HOME_ID TEXT,");
		sb.append("HOME_NAME TEXT,");
		sb.append("HOME_ICON TEXT,");
		sb.append("HOME_SIGN TEXT,");
		// 登录信息
		sb.append("LOGIN_PWD TEXT,");
		sb.append("LOGIN_TIME TEXT,");
		sb.append("LOGIN_AUTO INTEGER,");
		sb.append("LOGIN_REMPWD INTEGER");
		sb.append(");");
		return sb.toString();
	}

	/**
	 * 返回创建好友家庭信息表的SQL语句
	 *
	 * @return String
	 */
	public static String makeHomeFriendCreateTableSQL() {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE ");
		sb.append("home_friend");
		sb.append(" (");
		sb.append("HOME_ID TEXT ,");
		sb.append("FRIEND_HOME_ID TEXT,");
		sb.append("FRIEND_MOBILE TEXT,");
		sb.append("FRIEND_HOME_TYPE TEXT,");
		sb.append("HOME_MARK_NAME TEXT,");
		sb.append("HOME_NAME TEXT,");
		sb.append("ICON_PATH TEXT,");
		sb.append("SIGN TEXT");
		sb.append(");");
		return sb.toString();
	}

	/**
	 * 返回创建家庭好友家庭成员信息表的SQL语句
	 *
	 * @return String
	 */
	public static String makeHomeFriendUserCreateTableSQL() {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE ");
		sb.append("home_friend_user");
		sb.append(" (");
		// 这个 home_id , 有哪些用户
		sb.append("HOME_ID TEXT ,");
		sb.append("MOBILE TEXT,");
		sb.append("SIGN_NAME TEXT,");
		sb.append("SIGN TEXT,");
		sb.append("USER_TYPE TEXT,");
		sb.append("ICON_PATH TEXT");
		sb.append(");");

		return sb.toString();
	}

	/**
	 * 返回创建本地缓存家庭成员信息表的SQL语句
	 * @return String
	 */
	public static String makeHomeMemberUserCreateTableSQL() {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE ");
		sb.append("home_member_user");
		sb.append(" (");
		// 这个 home_id , 有哪些用户
		sb.append("HOME_ID TEXT ,");
		sb.append("HOME_NAME TEXT ,");
		sb.append("HOME_ICON TEXT ,");
		sb.append("HOME_SIGN TEXT ,");
		sb.append("USER_ID TEXT ,");
		sb.append("MOBILE TEXT,");
		sb.append("SIGN_NAME TEXT,");
		sb.append("SIGN TEXT,");
		sb.append("USER_TYPE TEXT,");
		sb.append("ICON_PATH TEXT,");
		sb.append("age_range TEXT,");
		sb.append("birthday_year INTEGER,");
		sb.append("height TEXT,");
		sb.append("gender TEXT,");
		sb.append("sport_level TEXT");
		sb.append(");");

		return sb.toString();
	}

	/**
	 * 返回创建本地缓存家庭数据版本信息表的SQL语句
	 * @return String
	 */
	public static String makeHomeVerCreateTableSQL() {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE ");
		sb.append("home_ver");
		sb.append(" (");
		// 登录用户的家庭 ID，一个手机，可能有多个登录用户，每个登录用户，可能有不同的 home_id
		sb.append("HOME_ID TEXT ,");
		// 这个 home_id , 有哪些用户
		sb.append("MEMBER_VER TEXT ,");
		sb.append("FRIEND_VER TEXT,");
		sb.append("DUMP_DEV_VER TEXT,");
		sb.append("DEV_VER TEXT");
		sb.append(");");

		return sb.toString();
	}

	/**
	 * 本地版本的 更新记录 , 仅记录出错信息
	 *
	 * @return String
	 */
	public static String makeHomeVerHisCreateTableSQL() {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE ");
		sb.append("home_ver_his");
		sb.append(" (");
		sb.append("HOME_ID TEXT ,");
		// VER_ID , 分为 MEMBER_VER, FRIEND_VER, DUMP_DEV_VER,DEV_VER
		sb.append("VER_ID TEXT ,");
		sb.append("VER_NO TEXT,");
		// 错误描述
		sb.append("ERR_DESC TEXT,");
		sb.append("UPDATE_TIME TEXT");
		sb.append(");");

		return sb.toString();
	}

	/**
	 * 转换成注册用的JSON对象
	 *
	 * @return JSONObject
	 * @throws JSONException
	 */
	public JSONObject toRegJSON() throws JSONException {
		JSONObject json = new JSONObject();

		json.put("name", this.name);
		json.put("mobile", this.mobile);
		json.put("password0", this.password);
		json.put("verf_code", this.verf_code);

		return json;
	}

	/**
	 * 用户对象转换成JSON对象
	 *
	 * @return JSONObject
	 */
	public JSONObject toJson() {
		JSONObject json = new JSONObject();

		try {
			json.put("user_id", user_id);
			json.put("name", name);
			json.put("icon_path", icon_path);
			json.put("sign", sign);
			json.put("mobile", mobile);
			json.put("user_type", user_type);
			json.put("email", email);
			json.put("gender", gender);
			json.put("height", height);
			json.put("age_range", age_range);
			json.put("sport_level", sport_level);
			json.put("age", age);



			json.put("app_id", app_id);
			json.put("session_id", session_id);
			json.put("channel_id", channel_id);

			json.put("email", "");

		} catch (JSONException e) {
			e.printStackTrace();
			// ILog.e(TAG, e);
		}

		return json;
	}

	/**
	 * 从JSON对象中解析出用户对象
	 *
	 * @param json_str JSON字符串格式的用户信息
	 * @return UserInfo
	 */
	public static UserInfo fromJson(String json_str) {
		if (TextUtils.isEmpty(json_str)) {
			return null;
		}

		UserInfo user = new UserInfo();
		try {
			JSONObject json = new JSONObject(json_str);

			user.user_id = json.optString("user_id");
			user.name = json.optString("name");
			user.icon_path = json.optString("icon_path");
			user.sign = json.optString("sign");
			user.mobile = json.optString("mobile");
			user.user_type = json.optString("user_type");
			user.email = json.optString("email");
			user.gender = json.optInt("gender");
			user.height = json.optInt("height");
			user.age_range = json.optInt("age_range");
			user.sport_level = json.optInt("sport_level");
			user.age = json.optInt("age");



			user.app_id = json.optString("app_id");
			user.channel_id = json.optString("channel_id");
			user.session_id = json.optString("session_id");

		} catch (JSONException e) {
			e.printStackTrace();
			// ILog.e(TAG, e);
		}

		return user;
	}

	/**
	 * 当前用户是否是管理员
	 *
	 * @return boolean
	 */
	public boolean isManager() {
		return USER_TYPE_MANAGER.equals(user_type);
	}

	@Override
	public String toString() {
		String str = "[UserInfo user_id : " + user_id + ", user_name : " + name + " ,home_id :"
				+ " ,password :" + password;
		str += ", sign = " + sign;
		str +=  " , user_type = " + user_type;
		str += ", gender = " + gender + " , height = " + height;
		str += ", sportsLevel = " + sport_level + ", autoLogin = " + auto_login + " , mobile = " + mobile;
		str += ", email = " + email + ", flagshare = " + flag_share + " , age_range = " + age_range;

		return str;
	}

}
