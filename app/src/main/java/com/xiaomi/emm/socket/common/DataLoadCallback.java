package com.xiaomi.emm.socket.common;

public interface DataLoadCallback {
	void onDataLoadStart();
	void onDataLoadFinished(BizRet bizRet);
}
