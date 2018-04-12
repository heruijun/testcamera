package com.my.testcamera;

import android.app.Application;

import com.aliyun.common.httpfinal.QupaiHttpFinal;

public class BaseApplication extends Application {

    private static BaseApplication mApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;

        System.loadLibrary("live-openh264");
        System.loadLibrary("QuCore-ThirdParty");
        System.loadLibrary("QuCore");
        System.loadLibrary("FaceAREngine");
        System.loadLibrary("AliFaceAREngine");

        QupaiHttpFinal.getInstance().initOkHttpFinal();
    }

    public static BaseApplication getApplication() {
        return mApplication;
    }
}
