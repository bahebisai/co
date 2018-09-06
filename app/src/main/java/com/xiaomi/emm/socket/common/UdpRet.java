package com.xiaomi.emm.socket.common;


import com.xiaomi.emm.socket.constant.AppErr;

// UDP 请求返回结果
public class UdpRet {
	// 业务返回码，只有0为成功
	public int ret = AppErr.OK;
	// 业务返回的出错字串
	public String msg = "";
	// 实际的业务数据类
	public String json_ret = "";

	public UdpRet() {
		ret = AppErr.OK;
	}

}
