package com.hrj.childfolioplugin.utils;

import android.os.Environment;

public class CFConstant {
    private final static String RECORD_CACHE_PATH = "/ChildFolio/media/record/";
    private final static String IMAGE_CACHE_PATH = "/ChildFolio/media/image/";
    private final static String VIDEO_CACHE_PATH = "/ChildFolio/media/video/";
    private final static String PDF_CACHE_PATH = "/ChildFolio/media/pdf/";
    public final static String RECORD_FULL_CACHE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + CFConstant.RECORD_CACHE_PATH;
    public final static String IMAGE_FULL_CACHE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + CFConstant.IMAGE_CACHE_PATH;
    public final static String VIDEO_FULL_CACHE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + CFConstant.VIDEO_CACHE_PATH;
    public final static String PDF_FULL_CACHE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + CFConstant.PDF_CACHE_PATH;

    public static boolean isSignUpStudentProcessActive = false;
    //内部测试版本
    public static boolean isInternalDevelopmentApp = false;
    /**
     * copy&edit 需求，除了学生和skill外，其它的都复制一份
     */
    public static boolean isCopyAndEditMoment = false;

    //年级名称
    public static final String[] GRADE_NAMES_EN = new String[]{"Age 0-1", "Age 1-2", "Age 2-3", "Age 3-4", "Age 4-5", "Age 5-6", "K (Kindergarten)", "Grade 1", "Grade 2", "Grade 3", "Grade 4", "Grade 5", "Grade 6", "Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12", "Mixed-age"};
    public static final String[] GRADE_NAMES = new String[]{"0-1岁", "1-2岁", "2-3岁", "3-4岁", "4-5岁", "5-6岁", "幼儿园", "1年级", "2年级", "3年级", "4年级", "5年级", "6年级", "7年级", "8年级", "9年级", "10年级", "11年级", "12年级", "混合年龄段"};
    //学生数量
    public static final String[] STUDENT_NUMS = new String[]{"1-10", "11-50", "51-100", "101-500", "501-1000", "1000+"};

    // 服务器位置
    public static boolean isUSServer;

    // 服务器位置
    public static boolean isLargeServer;

    public static boolean isAnji = false;

}
