package com.xiaomi.emm.socket.packet;


import com.xiaomi.emm.socket.utils.Formatter;

/**
 * 推送给 智能终端的消息包
 */
public class PushDevPacket extends BasicPacket {
	public static final int MAX_PACKET_DATA_LENGTH = 1024;

	public String app_id = "";
	public String channel_id = "";
	public String channel_type = "";
	public String session_id = "";
	// 推送消息类型
	public String push_msg_type;


	/**
	 * 生成 packet , 解析 json 内容
	 * @param info
	 */
	public void setPushMobilePacket(BasicPacket info) {
		this.msg_type = info.msg_type;
		this.msg_type_str = info.msg_type_str;
		this.seq = info.seq;
		this.content = info.content;
		this.content_len = info.content_len;
		this.content_str = info.content_str;
	}

	/**
	 * 生成 push包 ，服务器端使用
	 * @return
	 */
	public byte[] build_resp() {
		return _packet(PacketConstant.MSG_PUSH_DEV_RESP, seq, null);
	}

	public byte[] build_resp(int seq, String json_str){
		return _packet(PacketConstant.MSG_PUSH_DEV_RESP, seq, json_str);
	}

	/**
	 * 生成 push包 ，服务器端使用
	 * @param json_str
	 * @return
	 */
	public byte[] build_resp(String json_str) {
		return _packet(PacketConstant.MSG_PUSH_DEV_RESP, seq, json_str);
	}

	/**
	 * 生成 push包 ，服务器端使用
	 * @param ret
	 * @return
	 */
	public byte[] build_resp(int ret) {
		return _packet(PacketConstant.MSG_MOBILE_CMD_RESP, seq, ret, null);
	}

	@Override
	public String toString() {
		return "RegistePacket " + "("
				+ " |ver : " + ver
				+ " |seq : " + seq
				+ " |cmd : " + Formatter.byte2str(msg_type)
				+ " |ret : " + ret
				+ " |len : " + content_len
				+ " |data: " + content_str + "\n"
				+ " |=========== "
				+ " |channel_id : " + channel_id
				+ " |channel_type : " + channel_type
				+ " |app_id : " + app_id
				+ " |session_id : " + session_id
				+ " )";
	}



}

