package com.zoomtech.emm.socket.packet;

/**
 * 心跳包，仅有 10 个字节的包头
 */
public class HeartbeatPacket extends BasicPacket {

	public HeartbeatPacket() {
	}

	public byte[] build_req(int seq) {
		return _packet(PacketConstant.MSG_HEARTBEAT_REQ, seq, null);
	}

	public byte[] build_resp() {
		return _packet(PacketConstant.MSG_HEARTBEAT_RESP, seq, null);
	}

}
