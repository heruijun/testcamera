package com.hrj.childfolioplugin;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;

import com.aliyun.common.utils.CommonUtil;
import com.aliyun.common.utils.MySystemParams;
import com.aliyun.common.utils.StorageUtils;
import com.aliyun.querrorcode.AliyunErrorCode;
import com.aliyun.recorder.AliyunRecorderCreator;
import com.aliyun.recorder.supply.AliyunIClipManager;
import com.aliyun.recorder.supply.AliyunIRecorder;
import com.aliyun.recorder.supply.EncoderInfoCallback;
import com.aliyun.recorder.supply.RecordCallback;
import com.aliyun.struct.effect.EffectFilter;
import com.aliyun.struct.effect.EffectPaster;
import com.aliyun.struct.encoder.EncoderInfo;
import com.aliyun.struct.form.PreviewPasterForm;
import com.aliyun.struct.recorder.CameraParam;
import com.aliyun.struct.recorder.CameraType;
import com.aliyun.struct.recorder.FlashType;
import com.aliyun.struct.recorder.MediaInfo;
import com.bumptech.glide.Glide;
import com.hrj.childfolioplugin.utils.CFConstant;
import com.hrj.childfolioplugin.utils.Common;
import com.hrj.childfolioplugin.utils.IsTableUtils;
import com.hrj.childfolioplugin.utils.ListPopupWindow;
import com.hrj.childfolioplugin.utils.LocalImageHelper;
import com.hrj.childfolioplugin.utils.OpenGLTest;
import com.hrj.childfolioplugin.utils.OrientationDetector;
import com.hrj.childfolioplugin.utils.STMobileHumanActionNative;
import com.hrj.childfolioplugin.view.RoundAngleImageView;
import com.my.testcamera.R;
import com.qu.preview.callback.OnFrameCallBack;
import com.qu.preview.callback.OnTextureIdCallBack;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class TakePhotoActivity extends AppCompatActivity implements View.OnClickListener, GestureDetector.OnGestureListener,
        View.OnTouchListener, ScaleGestureDetector.OnScaleGestureListener {
    private static final String TAG = "CameraDemo";
    private static final String LOCAL_SETTING_NAME = "sdk_record_download_paster";

    private GLSurfaceView glSurfaceView;
    private ImageView switchCameraBtn, switchLightBtn, backBtn, compeleteBtn;
    private AliyunIRecorder recorder;
    private FlashType flashType = FlashType.ON;
    private CameraType cameraType = CameraType.BACK;
    private ImageView pasterView, pasterView2;
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private HorizontalScrollView scrollView;
    private RecyclerView mPhotoContainer;
    private PhotoAdapter mPhotoAdapter;
    private int maxPhotoNum = 9;

//    private STMobileHumanActionNative mSTHumanActionNative = new STMobileHumanActionNative();
    private static int TEST_VIDEO_WIDTH = 1080;
    private static int TEST_VIDEO_HEIGHT = 1920;
    private static final int MIN_RECORD_TIME = 500;
    private static final int MAX_RECORD_TIME = 15 * 1000;
    private static final int MAX_SWITCH_VELOCITY = 3000;

    private EffectPaster effect;
    private int filterIndex = 0;
    private int beautyLevel = 50;
    private float lastScaleFactor;
    private float scaleFactor;

    private boolean isRecording;

    private AliyunIClipManager clipManager;

    private OrientationDetector orientationDetector;
    private int rotation;

    private List<PreviewPasterForm> resources = new ArrayList<>();
    private List<LocalImageHelper.LocalFile> mSelectedFiles = new ArrayList<>();

    String[] eff_dirs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MySystemParams.getInstance().init(this);
        setRequestedOrientation(IsTableUtils.isTablet(getBaseContext()) ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_take_camera);
        initView();
        // getData();
        initOritationDetector();
        initSDK();
        // copyAssets();

    }

    private void initOritationDetector() {
        orientationDetector = new OrientationDetector(getApplicationContext());
        orientationDetector.setOrientationChangedListener(new OrientationDetector.OrientationChangedListener() {
            @Override
            public void onOrientationChanged() {
                rotation = getPictureRotation();

                recorder.setRotation(rotation);
            }
        });
    }

//    private void getData() {
//        if(mSelectedFiles != null) {
//            mSelectedFiles = (List<LocalImageHelper.LocalFile>) getIntent().getSerializableExtra("mSelectedFiles");
//            if (null != getIntent().getStringExtra("selectNum")) {
//                maxPhotoNum = 9 - Integer.parseInt(getIntent().getStringExtra("selectNum"));
//            } else
//                maxPhotoNum -= mSelectedFiles.size();
//            for (LocalImageHelper.LocalFile file : mSelectedFiles) {
//                if (file.type == 2) {
//                    maxPhotoNum += 1;
//                    break;
//                }
//            }
//            addPhoto();
//        }
//    }

    private void initSDK() {
        recorder = AliyunRecorderCreator.getRecorderInstance(this);
        recorder.setDisplayView(glSurfaceView);
        clipManager = recorder.getClipManager();
        clipManager.setMaxDuration(MAX_RECORD_TIME);
        clipManager.setMinDuration(MIN_RECORD_TIME);
        MediaInfo mediaInfo = new MediaInfo();
        mediaInfo.setVideoWidth(TEST_VIDEO_WIDTH);
        mediaInfo.setVideoHeight(TEST_VIDEO_HEIGHT);
        mediaInfo.setHWAutoSize(true);//硬编时自适应宽高为16的倍数
        recorder.setMediaInfo(mediaInfo);
        recorder.setLight(flashType);
        cameraType = recorder.getCameraCount() == 1 ? CameraType.BACK : cameraType;
        recorder.setCamera(cameraType);
        recorder.setBeautyLevel(beautyLevel);
        recorder.setFocusMode(CameraParam.FOCUS_MODE_CONTINUE);
        recorder.needFaceTrackInternal(true);
        recorder.setOnFrameCallback(new OnFrameCallBack() {
            @Override
            public void onFrameBack(byte[] bytes, int width, int height, Camera.CameraInfo info) {

            }

            @Override
            public Camera.Size onChoosePreviewSize(List<Camera.Size> supportedPreviewSizes,
                                                   Camera.Size preferredPreviewSizeForVideo) {
                return null;
            }

            @Override
            public void openFailed() {
            }
        });

        recorder.setRecordCallback(new RecordCallback() {

            @Override
            public void onComplete(boolean validClip, long clipDuration) {

            }

            @Override
            public void onFinish(String outputPath) {
                clipManager.deleteAllPart();//删除所有临时文件
            }

            @Override
            public void onProgress(final long duration) {

            }

            @Override
            public void onMaxDuration() {
                handleRecordStop();
            }

            @Override
            public void onError(int errorCode) {
                if (errorCode == AliyunErrorCode.ERROR_LICENSE_FAILED) {
                    // TODO: 2017/2/17
                }
                Log.e(TAG, "errorCode..." + errorCode);
                handleStopCallback(false, 0);
            }

            @Override
            public void onInitReady() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (effect != null) {
                            addEffectToRecord(effect.getPath());     //因为底层在onpause的时候会做资源回收，所以初始化完成的时候要做资源的恢复
                        }
                        String path = Common.QU_DIR + "maohuzi";
                        final EffectPaster paster = new EffectPaster(path);
                        paster.isTrack = false;

                    }
                });
            }

            @Override
            public void onDrawReady() {

            }

            @Override
            public void onPictureBack(final Bitmap bitmap) {

                File mFile = new File(CFConstant.IMAGE_FULL_CACHE_PATH, System.currentTimeMillis() + "_picture.jpg");
                try {
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(mFile));
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                    if (addFile(mFile)) {
                        mOpeHandler.sendEmptyMessage(TAKE_SUCCESS);
                        maxPhotoNum -= 1;
                    }
                    bos.flush();
                    bos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onPictureDataBack(final byte[] data) {

            }

        });

        recorder.setOnTextureIdCallback(new OnTextureIdCallBack() {
            @Override
            public int onTextureIdBack(int textureId, int textureWidth, int textureHeight, float[] matrix) {
                return textureId;
            }

            OpenGLTest test;

            @Override
            public int onScaledIdBack(int scaledId, int textureWidth, int textureHeight, float[] matrix) {
                if (test == null) {
                    test = new OpenGLTest();
                }
                return scaledId;
            }
        });
        recorder.setEncoderInfoCallback(new EncoderInfoCallback() {
            @Override
            public void onEncoderInfoBack(EncoderInfo info) {
                Log.d(TAG, info.toString());
            }
        });
        switchLightBtnState();
    }

    public static final int TAKE_SUCCESS = 0X10;
    private Handler mOpeHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case TAKE_SUCCESS:
                    addPhoto();
                    break;
            }
        }
    };

    /**
     * 添加文件
     *
     * @param tmpFile
     */
    private synchronized boolean addFile(File tmpFile) {
        if (maxPhotoNum > 0) {
            String filePath = tmpFile.getAbsolutePath();
            LocalImageHelper.LocalFile lFile = new LocalImageHelper.LocalFile(filePath, null, 0);
            mSelectedFiles.add(lFile);
            //通知系统相册刷新界面
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(tmpFile.getAbsolutePath()))));
            return true;
        }
        return false;
    }

    private int getPictureRotation() {
        int orientation = orientationDetector.getOrientation();
        int rotation = 90;
        if ((orientation >= 45) && (orientation < 135)) {
            rotation = 180;
        }
        if ((orientation >= 135) && (orientation < 225)) {
            rotation = 270;
        }
        if ((orientation >= 225) && (orientation < 315)) {
            rotation = 0;
        }
        if (cameraType == CameraType.FRONT) {
            if (rotation != 0) {
                rotation = 360 - rotation;
            }
        }
        Log.d("MyOrientationDetector", "generated rotation ..." + rotation);
        return rotation;
    }

    private void initPasterResource() {
        if (CommonUtil.hasNetwork(this)) {
        } else {
            initPasterResourceLocal();
        }
    }

    private void addConstantPaster() {
        String path = Common.QU_DIR + "maohuzi";
        File file = new File(path);
        File iconFile = new File(path + "/icon.png");
        if (file.exists() && iconFile.exists()) {
            PreviewPasterForm form = new PreviewPasterForm();
            form.setUrl(file.getAbsolutePath());
            form.setIcon(file.getAbsolutePath() + File.separator + "icon.png");
            form.setLocalRes(true);
            resources.add(0, form);
        }

    }

    private void initPasterResourceLocal() {
        File aseetFile = StorageUtils.getFilesDirectory(this);
        File[] files = null;
        if (aseetFile.isDirectory()) {
            files = aseetFile.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.isDirectory() && pathname.getName().contains("-")) {
                        return true;
                    }
                    return false;
                }
            });
        }
        if (files == null) {
            return;
        }
        for (File file : files) {
            PreviewPasterForm form = new PreviewPasterForm();
            form.setIcon(file.getAbsolutePath() + File.separator + "icon.png");
            String fileName = file.getName();
            String[] strs = fileName.split("-");
            if (strs.length == 2) {
                String name = strs[0];
                String id = strs[1];
                form.setName(name);
                form.setUrl(getLocalResUrl(id));
                try {
                    form.setId(Integer.parseInt(id));
                    resources.add(form);
                } catch (Exception e) {
                    continue;
                }
            } else {
                continue;
            }
        }
        addConstantPaster();
    }


//    private void copyAssets() {
//        new AsyncTask() {
//
//            @Override
//            protected Object doInBackground(Object[] params) {
//                Common.copyAll(TakePhotoActivity.this);
//                String path = StorageUtils.getCacheDirectory(TakePhotoActivity.this).getAbsolutePath() + File.separator + Common.QU_NAME + File.separator;
//                recorder.setFaceTrackInternalModelPath(path + "/model");
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Object o) {
//                initPasterResource();
//            }
//        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//    }

    private void initView() {
        glSurfaceView = (GLSurfaceView) findViewById(R.id.aliyun_preview);
        glSurfaceView.setOnTouchListener(this);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) glSurfaceView.getLayoutParams();
        Rect rect = new Rect();
        getWindowManager().getDefaultDisplay().getRectSize(rect);
        layoutParams.width = rect.width();
        layoutParams.height = rect.height();
        glSurfaceView.setLayoutParams(layoutParams);
        mPhotoContainer = (RecyclerView) findViewById(R.id.rv_img_container);
        scrollView = (HorizontalScrollView) findViewById(R.id.hsview);
        switchCameraBtn = (ImageView) findViewById(R.id.aliyun_switch_camera);
        switchCameraBtn.setOnClickListener(this);
        switchLightBtn = (ImageView) findViewById(R.id.aliyun_switch_light);
        switchLightBtn.setSelected(false);
        switchLightBtn.setActivated(true);
        switchLightBtn.setOnClickListener(this);
        pasterView = (ImageView) findViewById(R.id.aliyun_pasterView);
        pasterView.setOnClickListener(this);
        pasterView2 = (ImageView) findViewById(R.id.aliyun_pasterView2);
        pasterView2.setOnClickListener(this);
        backBtn = (ImageView) findViewById(R.id.aliyun_back);
        backBtn.setOnClickListener(this);
        compeleteBtn = (ImageView) findViewById(R.id.aliyun_complete);
        compeleteBtn.setOnClickListener(this);
        gestureDetector = new GestureDetector(this, this);
        scaleGestureDetector = new ScaleGestureDetector(this, this);
    }


    private String getLocalResUrl(String id) {
        SharedPreferences sharedPreferences = getSharedPreferences(LOCAL_SETTING_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(id, "");
    }

    private void addEffectToRecord(String path) {
        if (new File(path).exists()) {
            if (effect != null) {
                recorder.removePaster(effect);
            }
            effect = new EffectPaster(path);
            recorder.addPaster(effect);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        recorder.startPreview();
        recorder.setZoom(scaleFactor);
        if (orientationDetector != null && orientationDetector.canDetectOrientation()) {
            orientationDetector.enable();
        }
        if (!isActive) {
            isActive = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isRecording) {
            recorder.cancelRecording();
        }
        recorder.stopPreview();
    }

    private boolean isActive;

    @Override
    protected void onStop() {
        super.onStop();
        if (orientationDetector != null) {
            orientationDetector.disable();
        }
        if (!isForeground(this)) {
            isActive = false;
        }
    }

    /*判断应用是否在前台*/
    public static boolean isForeground(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recorder.destroy();
        // mSTHumanActionNative.destroyInstance();
        AliyunRecorderCreator.destroyRecorderInstance();
        if (orientationDetector != null) {
            orientationDetector.setOrientationChangedListener(null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    private void switchLightBtnState() {
        if (cameraType == CameraType.FRONT) {
            switchLightBtn.setVisibility(View.GONE);
        } else if (cameraType == CameraType.BACK) {
            switchLightBtn.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == switchCameraBtn) {
            int type = recorder.switchCamera();
            if (type == CameraType.BACK.getType()) {
                cameraType = CameraType.BACK;
            } else if (type == CameraType.FRONT.getType()) {
                cameraType = CameraType.FRONT;
            }
            switchLightBtnState();
        } else if (v == switchLightBtn) {
            if (flashType == FlashType.OFF) {
                flashType = FlashType.AUTO;
            } else if (flashType == FlashType.AUTO) {
                flashType = FlashType.ON;
            } else if (flashType == FlashType.ON) {
                flashType = FlashType.OFF;
            }
            switch (flashType) {
                case AUTO:
                    v.setSelected(false);
                    v.setActivated(true);
                    break;
                case ON:
                    v.setSelected(true);
                    v.setActivated(false);
                    break;
                case OFF:
                    v.setSelected(true);
                    v.setActivated(true);
                    break;
            }
            recorder.setLight(flashType);
        } else if (v == backBtn) {
            onBackPressed();
        } else if (v == compeleteBtn) {
            finishRecording();
        } else if (v == pasterView) {
            if (maxPhotoNum <= 0)
                return;
            recorder.takePhoto(true);
//                    recorder.takePicture(true);
            recorder.stopRecording();
            handleRecordStop();
        } else if (v == pasterView2) {
            if (maxPhotoNum <= 0)
                return;
            recorder.takePhoto(true);
//                    recorder.takePicture(true);
            recorder.stopRecording();
            handleRecordStop();
        }
    }

    @Override
    public void onBackPressed() {
        if (mSelectedFiles.isEmpty()) {
            finish();
        } else {
            if (IsTableUtils.isTablet(getContext())) {
                List<String> itemStr = new ArrayList<>();
                itemStr.add("此时退出，照片或视频将会丢失！");
                itemStr.add("退出");

                final ListPopupWindow listPopupWindow = new ListPopupWindow(getContext());
                listPopupWindow.showPopupWindow(backBtn, null, itemStr, new ListPopupWindow.ListPopupWindowListener() {
                    @Override
                    public void onItemClick(int position) {
                        switch (position) {
                            case 0:
                                break;
                            case 1:
                                finish();
                                break;
                        }
                    }
                });
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("此时退出，照片或视频将会丢失！");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    }

    public Context getContext() {
        return this;
    }

    private void addPhoto() {
        if (mPhotoAdapter == null) {
            LinearLayoutManager manager = new LinearLayoutManager(getBaseContext());
            manager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mPhotoContainer.setLayoutManager(manager);
            mPhotoAdapter = new PhotoAdapter(mSelectedFiles);
            mPhotoContainer.setAdapter(mPhotoAdapter);
        } else {
            mPhotoAdapter.updateFiles(mSelectedFiles);
        }
    }

    private void finishRecording() {
        //返回数据
        Intent intent = new Intent();
        intent.putExtra("files", (Serializable) mSelectedFiles);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean onDown(MotionEvent e) {

        return false;
    }


    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        float x = e.getX() / glSurfaceView.getWidth();
        float y = e.getY() / glSurfaceView.getHeight();
        recorder.setFocus(x, y);
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (e1.getPointerCount() > 1 || e2.getPointerCount() > 1) {
            return true;
        }
//        if (velocityX > MAX_SWITCH_VELOCITY) {
//            filterIndex++;
//            if (filterIndex >= eff_dirs.length) {
//                filterIndex = 0;
//            }
//        } else if (velocityX < -MAX_SWITCH_VELOCITY) {
//            filterIndex--;
//            if (filterIndex < 0) {
//                filterIndex = eff_dirs.length - 1;
//            }
//        } else {
//            return true;
//        }
//        EffectFilter effectFilter = new EffectFilter(eff_dirs[filterIndex]);
//        recorder.applyFilter(effectFilter);
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == glSurfaceView) {
            gestureDetector.onTouchEvent(event);
            scaleGestureDetector.onTouchEvent(event);
        }
        return true;
    }

    private void handleStopCallback(final boolean isValid, final long duration) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isRecording = false;
            }
        });
    }

    private void handleRecordStop() {
        pasterView.setVisibility(View.VISIBLE);
        switchCameraBtn.setVisibility(View.VISIBLE);
        if (cameraType == CameraType.BACK) {
            switchLightBtn.setVisibility(View.INVISIBLE);
        }
        backBtn.setVisibility(View.VISIBLE);
        compeleteBtn.setVisibility(View.VISIBLE);
        if (flashType == FlashType.ON && cameraType == CameraType.BACK) {
            recorder.setLight(FlashType.OFF);
        }

    }


    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        Log.e(TAG, "factor..." + detector.getScaleFactor());
        float factorOffset = detector.getScaleFactor() - lastScaleFactor;
        scaleFactor += factorOffset;
        lastScaleFactor = detector.getScaleFactor();
        if (scaleFactor < 0) {
            scaleFactor = 0;
        }
        if (scaleFactor > 1) {
            scaleFactor = 1;
        }
        recorder.setZoom(scaleFactor);
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        lastScaleFactor = detector.getScaleFactor();
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }


    private class PhotoAdapter extends RecyclerView.Adapter<GridViewHolder> {
        List<LocalImageHelper.LocalFile> files;

        private PhotoAdapter(List<LocalImageHelper.LocalFile> files) {
            getPhotoFiles(files);
        }

        public void updateFiles(List<LocalImageHelper.LocalFile> files) {
            getPhotoFiles(files);
            notifyDataSetChanged();
        }

        private void getPhotoFiles(List<LocalImageHelper.LocalFile> files) {
            this.files = new ArrayList<>();
            if (files.size() > 0) {
                for (LocalImageHelper.LocalFile localFile : mSelectedFiles) {
                    if (localFile.type == 0 || localFile.type == -1) {
                        this.files.add(localFile);
                    }
                }
            }
        }

        @Override
        public GridViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new GridViewHolder(LayoutInflater.from(getBaseContext()).inflate(R.layout.layout_moment_take_photo_item, parent, false));
        }

        @Override
        public void onBindViewHolder(GridViewHolder holder, int position) {
            holder.bindView(files.get(position), position);
        }

        @Override
        public int getItemCount() {
            return files.size();
        }
    }

    private class GridViewHolder extends RecyclerView.ViewHolder {
        RoundAngleImageView mImage;
        ImageView deleteImage;
        View itemView;

        public GridViewHolder(View itemView) {
            super(itemView);
            mImage = (RoundAngleImageView) itemView.findViewById(R.id.sv_photo);
            deleteImage = (ImageView) itemView.findViewById(R.id.img_delete);
            this.itemView = itemView;
        }

        private String[] getPhotoUrls() {
            List<String> urls = new ArrayList<>();
            for (LocalImageHelper.LocalFile lFile : mSelectedFiles) {
                if (lFile.type == 0 || lFile.type == -1) {
                    if (!TextUtils.isEmpty(lFile.originalPath)) {
                        urls.add(lFile.originalPath);
                    } else if (!TextUtils.isEmpty(lFile.previewPath)) {
                        urls.add(lFile.previewPath);
                    }
                }
            }
            String[] urlA = new String[urls.size()];
            for (int i = 0; i < urls.size(); i++) {
                urlA[i] = urls.get(i);
            }
            return urlA;
        }

        private void bindView(final LocalImageHelper.LocalFile localFile, int position) {
            if (!TextUtils.isEmpty(localFile.originalPath)) {
                Glide.with(getBaseContext()).load(localFile.originalPath)
                        .placeholder(R.mipmap.img_load).override(200, 200).centerCrop().into(mImage);
                if (position == 4) {
                    scrollView.smoothScrollBy(200, 0);
                }
            }
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Intent intent = new Intent(getBaseContext(), PhotoGalleryActivity.class);
//                    intent.putExtra("urls", getPhotoUrls());
//                    intent.putExtra("position", position);
//                    Bundle bundle = ActivityOptionsCompat.makeScaleUpAnimation(view, view.getWidth() / 2, view.getHeight() / 2, 0, 0).toBundle();
//                    startActivity(intent, bundle);
                }
            });
            deleteImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSelectedFiles.remove(localFile);
                    maxPhotoNum += 1;
                    mPhotoAdapter.updateFiles(mSelectedFiles);
                }
            });
        }
    }
}