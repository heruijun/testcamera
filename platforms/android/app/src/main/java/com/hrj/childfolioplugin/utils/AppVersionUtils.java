package com.hrj.childfolioplugin.utils;

import android.os.Build;

public class AppVersionUtils {

    public static boolean isUpLOLLIPOP() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }
}