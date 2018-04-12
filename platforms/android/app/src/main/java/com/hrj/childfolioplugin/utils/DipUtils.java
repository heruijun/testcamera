package com.hrj.childfolioplugin.utils;

import android.content.Context;

public class DipUtils {

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static float dp2px(Context context, float dp) {
        final float density = context.getResources().getDisplayMetrics().density;
        return dp * density;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dp2px(Context context, int dp) {
        final float density = context.getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static float px2dp(Context context, float pxValue) {
        final float density = context.getResources().getDisplayMetrics().density;
        return pxValue / density;
    }
}
