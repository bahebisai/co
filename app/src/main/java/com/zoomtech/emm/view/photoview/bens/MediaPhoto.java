package com.zoomtech.emm.view.photoview.bens;

import java.io.Serializable;

/**
 * Created by lenovo on 2017/11/8.
 */

public class MediaPhoto implements Serializable {
	private static final String TAG = "MediaPhoto";
	private String url;
	private boolean isCheck;

	public MediaPhoto(String url, boolean isCheck) {
		this.url = url;
		this.isCheck = isCheck;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setCheck(boolean check) {
		isCheck = check;
	}

	public String getUrl() {
		return url;
	}

	public boolean isCheck() {
		return isCheck;
	}
}
