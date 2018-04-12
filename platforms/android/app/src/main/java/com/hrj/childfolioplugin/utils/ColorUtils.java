package com.hrj.childfolioplugin.utils;

import android.graphics.Color;
import android.text.TextUtils;

public class ColorUtils {
    /**
     * 根据一个string 字符串解析对应的颜色值
     *
     * @param colorStr
     * @return
     */
    public static int generateColor(String colorStr) {
        int color = 0XFF9B9B9B;
        if (!TextUtils.isEmpty(colorStr)) {
            try {
                color = Color.parseColor(colorStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return color;
    }
}