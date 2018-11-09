package com.zoomtech.emm.socket.common;


import com.zoomtech.emm.socket.constant.AppErr;

public class BizRet {
	// 业务返回码，只有0为成功
	public int m_biz_code;
	// 业务返回的出错字串
	public String m_biz_msg = "";
	// 实际的业务数据类
	public Object m_biz_object = null ;

	public BizRet() {
		m_biz_code = AppErr.OK;
	}

	public boolean is_ok() {
		return m_biz_code == 0;
	}

	/**
	 * 是否是网络连接问题（连接服务器超时、socket读取超时）
	 * @return
	 */
	public boolean isHttpConnErr(){
		return m_biz_code == AppErr.ERR_HTTP_TIMEOUT
				|| m_biz_code == AppErr.ERR_HTTP_CONNECT_TIMEOUT
				|| m_biz_code == AppErr.ERR_HTTP_IO_ERR;
	}

	/**
	 * 设备通讯出错
	 * @return
	 */
	public boolean isDevCommunicationErr(){
		return m_biz_code > 0 && m_biz_code < 100;
	}

}
