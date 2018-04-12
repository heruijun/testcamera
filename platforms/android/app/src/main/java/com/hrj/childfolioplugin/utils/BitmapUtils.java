package com.hrj.childfolioplugin.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.text.TextUtils;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapUtils {
    /**
     * 将bitmap存储到本地
     *
     * @param bitmap
     * @param filePath
     * @return
     */
    public static String saveBitmap2Local(Bitmap bitmap, String filePath) {
        return saveBitmap2Local(bitmap, filePath, 100);
    }

    /**
     * @param bitmap
     * @param filePath
     * @return
     */
    public static String saveBitmap2Local(Bitmap bitmap, String filePath, String fileName) {
        return saveBitmap2Local(bitmap, filePath, fileName, 100, ".jpg");
    }

    /**
     * @param bitmap
     * @param filePath
     * @param quality  图片质量
     * @return
     */
    public static String saveBitmap2Local(Bitmap bitmap, String filePath, int quality) {
        return saveBitmap2Local(bitmap, filePath, null, quality, "_picture.jpg");
    }

    /**
     * 链接图片
     *
     * @param bitmap
     * @param filePath
     * @return
     */
    public static String saveLinkBitmap2Local(Bitmap bitmap, String filePath) {
        return saveBitmap2Local(bitmap, filePath, null, 100, "_link.jpg");
    }

    /**
     * 链接图片
     *
     * @param bitmap
     * @param filePath
     * @return
     */
    public static String saveTextBitmap2Local(Bitmap bitmap, String filePath) {
        return saveBitmap2Local(bitmap, filePath, null, 100, "_text.jpg");
    }

    /**
     * 链接图片
     *
     * @param bitmap
     * @param filePath
     * @return
     */
    public static String saveDrawingBitmap2Local(Bitmap bitmap, String filePath) {
        return saveBitmap2Local(bitmap, filePath, null, 100, "_drawing.jpg");
    }

    /**
     * * @param bitmap
     *
     * @param filePath
     * @param quality
     * @param suffixName 有四种后缀，学生编辑moment的根据后缀去判断修改类型
     *                   _text:文字图片
     *                   _picture:默认（老师统一用这个）
     *                   _link:链接生成的图片
     *                   _drawing:绘制的图片
     * @param fileName
     * @return
     */
    private static String saveBitmap2Local(Bitmap bitmap, String filePath, String fileName, int quality, String suffixName) {
        File dir = new File(filePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String filePullPath;
        if (TextUtils.isEmpty(fileName)) {
            filePullPath = dir.getPath() + "/" + System.currentTimeMillis() + suffixName;
        } else {
            filePullPath = dir.getPath() + "/" + fileName + suffixName;
        }

        File file = new File(filePullPath);
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filePullPath;
    }


    /**
     * 图片尺寸缩放，最大宽度不超过targetWidth
     */
    public static Bitmap resizeImage(Bitmap bitmap, int targetWidth) {
        Bitmap originBitmap = bitmap;
        int width = originBitmap.getWidth();
        int height = originBitmap.getHeight();

        if (width > targetWidth) {
            float scale = ((float) targetWidth) / width;
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);

            Bitmap resizedBitmap = originBitmap.createBitmap(originBitmap, 0, 0, width, height, matrix, true);

            return resizedBitmap;
        } else {
            return originBitmap;
        }
    }

    /**
     * 图片旋转，根据照片Exif信息
     */
    public static Bitmap rotateImage(String src) {
        Bitmap bitmap = BitmapFactory.decodeFile(src);
        try {
            ExifInterface exif = new ExifInterface(src);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.setScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.setRotate(180);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.setRotate(180);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    matrix.setRotate(90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.setRotate(90);
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    matrix.setRotate(-90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.setRotate(-90);
                    break;
                case ExifInterface.ORIENTATION_NORMAL:
                case ExifInterface.ORIENTATION_UNDEFINED:
                default:
                    return bitmap;
            }

            try {
                Bitmap oriented = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                bitmap.recycle();
                return oriented;
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                return bitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * @param srcFullPath
     * @param destDir
     * @return
     */
    public static String createThumbnail(String srcFullPath, String destDir) {
        Bitmap bitmap = rotateImage(srcFullPath);
        Bitmap thumBmp = resizeImage(bitmap, 400);
        return saveBitmap2Local(thumBmp, destDir);
    }

    /**
     * @param srcFullPath
     * @param destDir
     * @return
     */
    public static String createThumbnailwithSize(String srcFullPath, String destDir,int size) {
        Bitmap bitmap = rotateImage(srcFullPath);
        Bitmap thumBmp = resizeImage(bitmap, size);
        return saveBitmap2Local(thumBmp, destDir);
    }


    /**
     * 将bitmap转为base64
     *
     * @param bitmap
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                baos.flush();
                baos.close();
                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static Bitmap createBitmap(int width, int height) {
        int[] colors = new int[width * height];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = ColorUtils.generateColor("#FFFFFF");
        }
        return Bitmap.createBitmap(colors, width, height, Bitmap.Config.ARGB_8888);
    }
}
