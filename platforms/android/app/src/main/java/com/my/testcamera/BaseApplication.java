package com.my.testcamera;

import android.app.Application;

import com.aliyun.common.httpfinal.QupaiHttpFinal;

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        System.loadLibrary("live-openh264");
        System.loadLibrary("QuCore-ThirdParty");
        System.loadLibrary("QuCore");
        System.loadLibrary("FaceAREngine");
        System.loadLibrary("AliFaceAREngine");

        QupaiHttpFinal.getInstance().initOkHttpFinal();
    }
}
