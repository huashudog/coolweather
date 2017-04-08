package com.coolweather.app.util;

public interface HttpCallbackListener {

	public void onError(Exception e);

	public void onFinish(String response);

}
