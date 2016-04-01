package com.lwn314.coolweather.util;

/**
 * Created by lwn31 on 2016/4/1.
 */
public interface HttpCallbackListener {
    void onFinish(String response);

    void onError(Exception e);
}
