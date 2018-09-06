package com.xiaomi.emm.socket.common;

public class HttpRet {
	// HTTP 返回码
	public int m_http_code;
	// 正常返回的内容
	public String m_http_content = "";

	public HttpRet() {
		m_http_code = 200;
	}

	public boolean is_ok() {
		return m_http_code == 200;
	}
}
