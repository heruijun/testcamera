package com.hrj.childfolioplugin.utils;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.my.testcamera.BaseApplication;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalImageHelper {
    private static LocalImageHelper instance;
    //照片大图遍历字段
    private static final String[] STORE_IMAGES = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.ORIENTATION
    };
    //照片小图遍历字段
    private static final String[] THUMBNAIL_STORE_IMAGE = {
            MediaStore.Images.Thumbnails._ID,
            MediaStore.Images.Thumbnails.DATA
    };
    //视频
    private static final String[] STORE_VIDEO = {
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.ALBUM,
            MediaStore.Video.Media.DISPLAY_NAME,
    };
    //照片小图遍历字段
    private static final String[] THUMBNAIL_STORE_VIDEO = {
            MediaStore.Video.Thumbnails._ID,
            MediaStore.Video.Thumbnails.DATA
    };

    final List<LocalFile> photoPaths = new ArrayList<>();
    final List<LocalFile> videoPaths = new ArrayList<>();

    final Map<String, List<LocalFile>> photoFolders = new HashMap<>();
    final Map<String, List<LocalFile>> videoFolders = new HashMap<>();

    private LocalImageHelper() {

    }

    public Map<String, List<LocalFile>> getPhotoFolderMap() {
        return photoFolders;
    }

    public Map<String, List<LocalFile>> getVideoFolderMap() {
        return videoFolders;
    }

    public synchronized static LocalImageHelper getInstance() {
        if (instance == null) {
            instance = new LocalImageHelper();
        }
        return instance;
    }

    public boolean isPhotoLoaded() {
        return photoPaths.size() > 0;
    }

    public boolean isVideoLoaded() {
        return videoPaths.size() > 0;
    }

    public void reloadData() {
        photoPaths.clear();
        videoPaths.clear();
        photoFolders.clear();
        videoFolders.clear();
        loadImage();
        loadVideo();
    }

    private boolean isRunning = false;

    public void loadVideo() {
        if (isRunning) {
            return;
        }
        if (isVideoLoaded()) {
            return;
        }
        isRunning = true;
        Cursor cursor = BaseApplication.getApplication().getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                STORE_VIDEO, null, null, MediaStore.Video.Media.DATE_TAKEN + " DESC");
        if (null != cursor) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);//视频ID
                String path = cursor.getString(1);//视频地址
                File file = new File(path);
                //判断大图是否存在
                if (file.exists()) {
                    //缩略图
                    String thumbUri = getVideoPreview(id, path);
                    if (TextUtils.isEmpty(thumbUri)) {//没有缩略图
                        continue;
                    }
                    //获取目录名
                    String folder = file.getParentFile().getName();
                    LocalFile localFile = new LocalFile();
                    localFile.originalPath = path;//原始路径，上传用
                    localFile.previewPath = thumbUri;//缩略图，显示用，
                    localFile.type = 1;
                    //创建缩略图，上传用
                    videoPaths.add(localFile);
                    //判断文件夹是否已经存在
                    if (videoFolders.containsKey(folder)) {
                        videoFolders.get(folder).add(localFile);
                    } else {
                        List<LocalFile> files = new ArrayList<>();
                        files.add(localFile);
                        videoFolders.put(folder, files);
                    }
                }
            }
            cursor.close();
        }
        isRunning = false;
    }

    /**
     * 获取视频缩略图
     *
     * @param id
     * @return
     */
    private String getVideoPreview(int id, String filePath) {
        //获取大图的缩略图
        String fileName = new File(filePath).getName();
        File file = new File(CFConstant.IMAGE_FULL_CACHE_PATH, fileName + ".jpg");
        if (file.exists()) {
            return file.getAbsolutePath();
        } else {
            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
            String thumPath = BitmapUtils.saveBitmap2Local(bitmap, CFConstant.IMAGE_FULL_CACHE_PATH, fileName);
            bitmap.recycle();
            return thumPath;
        }
    }

    /**
     * 加载图片
     */
    public void loadImage() {
        if (isRunning) {
            return;
        }

        if (isPhotoLoaded()) {
            return;
        }
        isRunning = true;
        //获取大图的游标
        Cursor cursor = BaseApplication.getApplication().getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,  // 大图URI
                STORE_IMAGES,   // 字段
                null,         // No where clause
                null,         // No where clause
                MediaStore.Images.Media.DATE_TAKEN + " DESC"); //根据时间升序
        if (cursor == null) {
            return;
        }
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);//大图ID
            String path = cursor.getString(1);//大图路径
            File file = new File(path);
            //判断大图是否存在
            if (file.exists()) {
                //获取目录名
                String folder = file.getParentFile().getName();
                LocalFile localFile = new LocalFile();
                localFile.originalPath = path;
                localFile.previewPath = path;
                int degree = cursor.getInt(2);
                if (degree != 0) {
                    degree = degree + 180;
                }
                localFile.setOrientation(360 - degree);

                photoPaths.add(localFile);
                //判断文件夹是否已经存在
                if (photoFolders.containsKey(folder)) {
                    photoFolders.get(folder).add(localFile);
                } else {
                    List<LocalFile> files = new ArrayList<>();
                    files.add(localFile);
                    photoFolders.put(folder, files);
                }
            }
        }
        cursor.close();
        isRunning = false;
    }

    public static class LocalFile implements Serializable {
        public String originalPath;//原图URI(视频地址,音频地址)sd上面的地址
        public String thumbnailPath;//缩略图上传地址sd上面的地址
        public String previewPath;//缩略图URI  预览的地址
        public String awsOriginalPath;//原始资源上传成功后，aws地址
        public String awsThumbnailPath;//缩略图上传成功后，aws地址
        public int orientation;//图片旋转角度
        public int type;//图片0(默认为图片)，视频1，音频2,-1原始图片资源
        public int finishState;
        private int currentState;//是否上传完成，每次上传成功一次，state+1;

        public boolean isFinishUpload() {
            switch (type) {
                case -1:
                case 1://视频，默认有缩略图和原图
                case 0:
                    finishState = 2;
                    break;
                case 2://音频
                    finishState = 1;
                    break;
            }
            return currentState >= finishState;
        }

        public void addCurrentState() {
            synchronized (LocalFile.class) {
                currentState += 1;
            }
        }

        public void recycleCurrentState() {
            synchronized (LocalFile.class) {
                currentState = 0;
            }
        }

        public int getCurrentState() {
            return currentState;
        }

        public LocalFile() {
        }

        /**
         * @param originalUri  原图URI(视频地址,音频地址)
         * @param thumbnailUri
         * @param type         图片0(默认为图片)，视频1，音频2
         */
        public LocalFile(String originalUri, String thumbnailUri, int type) {
            this.originalPath = originalUri;
            this.previewPath = thumbnailUri;
            this.type = type;
        }

        public LocalFile(String thumbnailUri) {
            this.previewPath = thumbnailUri;
        }

        public String getPreviewPath() {
            return previewPath;
        }

        public void setPreviewPath(String previewPath) {
            this.previewPath = previewPath;
        }

        public String getOriginalPath() {
            return originalPath;
        }

        public void setOriginalPath(String originalPath) {
            this.originalPath = originalPath;
        }


        public int getOrientation() {
            return orientation;
        }

        public void setOrientation(int exifOrientation) {
            orientation = exifOrientation;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            LocalFile file = (LocalFile) o;

            if (type != file.type) return false;
            if (originalPath != null ? !originalPath.equals(file.originalPath) : file.originalPath != null)
                return false;
            return previewPath != null ? previewPath.equals(file.previewPath) : file.previewPath == null;

        }

        @Override
        public int hashCode() {
            int result = originalPath != null ? originalPath.hashCode() : 0;
            result = 31 * result + (previewPath != null ? previewPath.hashCode() : 0);
            result = 31 * result + type;
            return result;
        }
    }
}