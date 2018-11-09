package com.zoomtech.emm.socket.packet;


import android.util.Log;

import com.zoomtech.emm.socket.bean.UserInfo;
import com.zoomtech.emm.socket.utils.Formatter;

import org.json.JSONObject;


// 手机/PAD 端 连接注册包
public class MobileRegistePacket extends BasicPacket {
	final static String TAG = "MobileRegistePacket";

	public String user_id = "";
	public String user_name = "";
	public String user_type = "";
	public String icon_path = "";
	public String home_id = "";
	public String channel_id = "";
	public String channel_type = "";
	public String app_id = "";
	public String phone_session_id = "";
	public String v_source = "";
	public int product_code = 0;





	public MobileRegistePacket() {
	}

	// 生成 packet , 解析 json 内容
	public void setRegPacket(BasicPacket info) {
		this.msg_type = info.msg_type;
		this.msg_type_str = info.msg_type_str;
		this.seq = info.seq;
		this.content = info.content;
		this.content_len = info.content_len;
		this.content_str = info.content_str;
		this.ret = info.ret;

		// 登录出错
		if (ret != 0) {
			Log.w(TAG, "setRegPacket() error !!! ret = " + ret);
			return;
		}
		if (msg_type == PacketConstant.MSG_MOBILE_REGISTE_REQ) {
            parse_req_json();
        } else if (msg_type == PacketConstant.MSG_MOBILE_REGISTE_RESP) {
            parse_resp_json();
        }
	}

	// 从客户端 发出的 注册回应包 json 解析
	public String parse_req_json() {
		String str = null;
		try {
			// JSONObject json = new JSONObject(content_str);
			// channel_id = json.getString("channel_id");
			// channel_type = json.getString("channel_type");
			// app_id = json.getString("app_id");
			// dev_ip = json.getString("internal_ip");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return str;
	}

	// 从 服务器端 返回的 注册回应包中 json 解析
	public String parse_resp_json() {
		String str = null;
		try {
			JSONObject json = new JSONObject(content_str);
			channel_id = json.getString("channel_id");
			channel_type = json.getString("channel_type");
			app_id = json.getString("app_id");
			phone_session_id = json.getString("session_id");
			home_id = json.getString("home_id");

			user_id = json.getString("user_id");
			user_name = json.getString("name");
			user_type = json.getString("type");
			icon_path = json.getString("icon_path");

			product_code = json.optInt("product_code", 0);

			v_source = json.optString("v_source");

			Log.w(TAG, "parse_resp_json() 收到的登录返回包=" + json.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return str;
	}

	// 生成注册 请求包 ，客户端使用
	public byte[] build_req(String channel_id, String channel_type, int app_id, String ip, int seq,
                            String ver_name, String model, int net_type, UserInfo user) {
		String json_str = null;
		JSONObject json = new JSONObject();
		try {
			json.put("channel_id", channel_id);
			json.put("channel_type", channel_type);
			json.put("model", model);
			json.put("net_type", net_type + "");
			json.put("app_id", app_id);
			json.put("internal_ip", ip);
			json.put("ver_name", ver_name);

			if (user != null) {
				json.put("mobile", user.mobile);
				json.put("passwd", user.password);
			} else {
				json.put("mobile", "");
				json.put("passwd", "");
			}
			json_str = json.toString();
			Log.w(TAG, "build_req() json_str = " + json_str);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return _packet(PacketConstant.MSG_MOBILE_REGISTE_REQ, seq, json_str);
	}





	// 生成注册 回应包 ，服务器端使用
	public byte[] build_resp(String session_id) {

		JSONObject json = new JSONObject();
		String json_str = null;
		try {
			json.put("channel_id", channel_id);
			json.put("channel_type", channel_type);
			json.put("app_id", app_id);
			json.put("session_id", session_id);
			json_str = json.toString();
			Log.w(TAG, "build_resp() json_str = " + json_str);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return _packet(PacketConstant.MSG_MOBILE_REGISTE_RESP, seq, json_str);
	}

	@Override
	public String toString() {
		return "RegistePacket " + "(" + " |ver : " + ver
				+ " |seq : "
				+ seq
				+
				// msg_type_str
				" |cmd : " + Formatter.byte2str(msg_type) + " |ret : " + ret + " |len : " + content_len
				+ " |data: " + content_str + "\n" + " |=========== " + " |channel_id : " + channel_id
				+ " |channel_type : " + channel_type + " |app_id : " + app_id + " |session_id : "
				+ phone_session_id + " )";
	}
}
