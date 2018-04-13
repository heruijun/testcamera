package com.hrj.childfolioplugin;

import android.animation.Animator;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aliyun.common.global.Version;
import com.aliyun.common.utils.CommonUtil;
import com.aliyun.recorder.AliyunRecorderCreator;
import com.aliyun.recorder.supply.AliyunIClipManager;
import com.aliyun.recorder.supply.AliyunIRecorder;
import com.aliyun.recorder.supply.RecordCallback;
import com.aliyun.struct.common.AliyunVideoParam;
import com.aliyun.struct.common.ScaleMode;
import com.aliyun.struct.common.VideoQuality;
import com.aliyun.struct.effect.EffectFilter;
import com.aliyun.struct.recorder.CameraParam;
import com.aliyun.struct.recorder.CameraType;
import com.aliyun.struct.recorder.FlashType;
import com.aliyun.struct.recorder.MediaInfo;
import com.aliyun.struct.snap.AliyunSnapVideoParam;
import com.hrj.childfolioplugin.utils.CFConstant;
import com.hrj.childfolioplugin.utils.IsTableUtils;
import com.hrj.childfolioplugin.utils.ListPopupWindow;
import com.hrj.childfolioplugin.utils.OrientationDetector;
import com.hrj.childfolioplugin.view.AliyunSVideoGlSurfaceView;
import com.hrj.childfolioplugin.view.CircleProgressView;
import com.hrj.childfolioplugin.view.RecordTimelineView;
import com.my.testcamera.R;
import com.qu.preview.callback.OnFrameCallBack;
import com.qu.preview.callback.OnTextureIdCallBack;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class VideoRecorderActivity extends Activity implements View.OnClickListener, View.OnTouchListener, ScaleGestureDetector.OnScaleGestureListener, GestureDetector.OnGestureListener {

    private static final int EFFECT_BEAUTY_LEVEL = 80;
    private static final int TIMELINE_HEIGHT = 20;

    private static final int MAX_SWITCH_VELOCITY = 2000;
    private static final float FADE_IN_START_ALPHA = 0.3f;
    private static final int FILTER_ANIMATION_DURATION = 1000;


    public static final String NEED_GALLERY = "need_gallery";


    public static final String OUTPUT_PATH = "output_path";

    private static final int REQUEST_CROP = 2001;

    public static final String RESULT_TYPE = "result_type";
    public static final int RESULT_TYPE_CROP = 4001;
    public static final int RESULT_TYPE_RECORD = 4002;


    private int mResolutionMode;
    private int minDuration;
    private int maxDuration;
    private int gop;
    private int mBitrate;
    private int mBeautyLevel;
    private int recordMode;
    private VideoQuality videoQuality = VideoQuality.HD;
    private int mRatioMode = AliyunSnapVideoParam.RATIO_MODE_3_4;
    private int mSortMode = AliyunSnapVideoParam.SORT_MODE_MERGE;
    private AliyunIRecorder mRecorder;
    private AliyunIClipManager mClipManager;
    private AliyunSVideoGlSurfaceView mGlSurfaceView;
    private boolean isBeautyOn = true;
    private boolean isSelected = false;
    private RecordTimelineView mRecordTimelineView;
    private ImageView mSwitchRatioBtn, mSwitchBeautyBtn, mSwitchCameraBtn, mSwitchLightBtn, mBackBtn, mRecordBtn, mDeleteBtn, mCompleteBtn, mGalleryBtn;
    private TextView mRecordTimeTxt, mRecordShowtime;
    private CircleProgressView circleProgressBar;
    private LinearLayout mRecordLayout;
    private FrameLayout mToolBar;
    private FlashType mFlashType = FlashType.OFF;
    private CameraType mCameraType = CameraType.FRONT;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private float scaleFactor;
    private float lastScaleFactor;
    private float exposureCompensationRatio = 0.5f;
    private boolean isOnMaxDuration;
    private boolean isOpenFailed;
    private boolean isRecording = false;
    private AliyunVideoParam mVideoParam;
    private OrientationDetector orientationDetector;
    private int tintColor, timelineDelBgColor, timelineBgColor, timelinePosY, lightDisableRes, lightSwitchRes;
    private long downTime;
    private String[] filterList;
    private int filterIndex = 0;
    private TextView mFilterTxt;
    private boolean isNeedClip;
    private boolean isNeedGallery;
    private boolean isRecordError;
    private MediaScannerConnection msc;

    private int mFrame = 25;
    private ScaleMode mCropMode = ScaleMode.PS;
    private int minCropDuration = 2000;
    private int maxVideoDuration = 10000;
    private int minVideoDuration = 2000;

    private int galleryVisibility;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            //透明状态栏
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        }
        setRequestedOrientation(IsTableUtils.isTablet(getBaseContext()) ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_take_video);
        getStyleParam();
        initOrientationDetector();
        getData();
        initView();
        initSDK();
        reSizePreview();
        msc = new MediaScannerConnection(this, null);
        msc.connect();
    }

    public static void startRecordForResult(Activity activity, int requestCode, AliyunSnapVideoParam param) {
        Intent intent = new Intent(activity, VideoRecorderActivity.class);
        intent.putExtra(AliyunSnapVideoParam.VIDEO_RESOLUTION, param.getResolutionMode());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_RATIO, param.getRatioMode());
        intent.putExtra(AliyunSnapVideoParam.RECORD_MODE, param.getRecordMode());
        intent.putExtra(AliyunSnapVideoParam.FILTER_LIST, param.getFilterList());
        intent.putExtra(AliyunSnapVideoParam.BEAUTY_LEVEL, param.getBeautyLevel());
        intent.putExtra(AliyunSnapVideoParam.BEAUTY_STATUS, param.getBeautyStatus());
        intent.putExtra(AliyunSnapVideoParam.CAMERA_TYPE, param.getCameraType());
        intent.putExtra(AliyunSnapVideoParam.FLASH_TYPE, param.getFlashType());
        intent.putExtra(AliyunSnapVideoParam.NEED_CLIP, param.isNeedClip());
        intent.putExtra(AliyunSnapVideoParam.MAX_DURATION, param.getMaxDuration());
        intent.putExtra(AliyunSnapVideoParam.MIN_DURATION, param.getMinDuration());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_QUALITY, param.getVideoQuality());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_GOP, param.getGop());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_BITRATE, param.getVideoBitrate());
        intent.putExtra(AliyunSnapVideoParam.SORT_MODE, param.getSortMode());


        intent.putExtra(AliyunSnapVideoParam.VIDEO_FRAMERATE, param.getFrameRate());
        intent.putExtra(AliyunSnapVideoParam.CROP_MODE, param.getScaleMode());
        intent.putExtra(AliyunSnapVideoParam.MIN_CROP_DURATION, param.getMinCropDuration());
        intent.putExtra(AliyunSnapVideoParam.MIN_VIDEO_DURATION, param.getMinVideoDuration());
        intent.putExtra(AliyunSnapVideoParam.MAX_VIDEO_DURATION, param.getMaxVideoDuration());
        intent.putExtra(AliyunSnapVideoParam.SORT_MODE, param.getSortMode());
        activity.startActivityForResult(intent, requestCode);
    }

    public static void startRecord(Context context, AliyunSnapVideoParam param) {
        Intent intent = new Intent(context, VideoRecorderActivity.class);
        intent.putExtra(AliyunSnapVideoParam.VIDEO_RESOLUTION, param.getResolutionMode());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_RATIO, param.getRatioMode());
        intent.putExtra(AliyunSnapVideoParam.RECORD_MODE, param.getRecordMode());
        intent.putExtra(AliyunSnapVideoParam.FILTER_LIST, param.getFilterList());
        intent.putExtra(AliyunSnapVideoParam.BEAUTY_LEVEL, param.getBeautyLevel());
        intent.putExtra(AliyunSnapVideoParam.BEAUTY_STATUS, param.getBeautyStatus());
        intent.putExtra(AliyunSnapVideoParam.CAMERA_TYPE, param.getCameraType());
        intent.putExtra(AliyunSnapVideoParam.FLASH_TYPE, param.getFlashType());
        intent.putExtra(AliyunSnapVideoParam.NEED_CLIP, param.isNeedClip());
        intent.putExtra(AliyunSnapVideoParam.MAX_DURATION, param.getMaxDuration());
        intent.putExtra(AliyunSnapVideoParam.MIN_DURATION, param.getMinDuration());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_QUALITY, param.getVideoQuality());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_GOP, param.getGop());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_BITRATE, param.getVideoBitrate());
        intent.putExtra(AliyunSnapVideoParam.SORT_MODE, param.getSortMode());


        intent.putExtra(AliyunSnapVideoParam.VIDEO_FRAMERATE, param.getFrameRate());
        intent.putExtra(AliyunSnapVideoParam.CROP_MODE, param.getScaleMode());
        intent.putExtra(AliyunSnapVideoParam.MIN_CROP_DURATION, param.getMinCropDuration());
        intent.putExtra(AliyunSnapVideoParam.MIN_VIDEO_DURATION, param.getMinVideoDuration());
        intent.putExtra(AliyunSnapVideoParam.MAX_VIDEO_DURATION, param.getMaxVideoDuration());
        intent.putExtra(AliyunSnapVideoParam.SORT_MODE, param.getSortMode());
        context.startActivity(intent);
    }

    public static String getVersion() {
        return Version.VERSION;
    }

    private void getStyleParam() {
        TypedArray a = obtainStyledAttributes(new int[]{
                R.attr.qusnap_tint_color, R.attr.qusnap_timeline_del_backgound_color,
                R.attr.qusnap_timeline_backgound_color, R.attr.qusnap_time_line_pos_y,
                R.attr.qusnap_switch_light_icon_disable, R.attr.qusnap_switch_light_icon,
                R.attr.qusnap_gallery_icon_visibility});
        tintColor = a.getResourceId(0, R.color.aliyun_record_fill_progress);
        timelineDelBgColor = a.getResourceId(1, android.R.color.holo_red_dark);
        timelineBgColor = a.getResourceId(2, R.color.aliyun_editor_overlay_line);
        timelinePosY = (int) a.getDimension(3, 100f);
        lightDisableRes = a.getResourceId(4, R.mipmap.icon_light_dis);
        lightSwitchRes = a.getResourceId(5, R.drawable.switch_light_selector);
        galleryVisibility = a.getInt(6, 0);
        a.recycle();
    }

    private void reSizePreview() {
        RelativeLayout.LayoutParams previewParams = null;
        RelativeLayout.LayoutParams timeLineParams = null;
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        switch (mRatioMode) {
            case AliyunSnapVideoParam.RATIO_MODE_1_1:
                previewParams = new RelativeLayout.LayoutParams(screenWidth, screenWidth);
                previewParams.addRule(RelativeLayout.BELOW, R.id.aliyun_tools_bar);
                timeLineParams = new RelativeLayout.LayoutParams(screenWidth, TIMELINE_HEIGHT);
                timeLineParams.addRule(RelativeLayout.BELOW, R.id.aliyun_preview);
                timeLineParams.topMargin = -timelinePosY;
                mRecordTimelineView.setColor(tintColor, timelineDelBgColor, R.color.qupai_black_opacity_70pct, timelineBgColor);
                break;
            case AliyunSnapVideoParam.RATIO_MODE_3_4:
                int barHeight = getVirtualBarHeight();
                float ratio = (float) screenHeight / screenWidth;
                previewParams = new RelativeLayout.LayoutParams(screenWidth, screenWidth * 4 / 3);
                if (barHeight > 0 || ratio < (16f / 9.2f)) {
                } else {
                    previewParams.addRule(RelativeLayout.BELOW, R.id.aliyun_tools_bar);
                }
                timeLineParams = new RelativeLayout.LayoutParams(screenWidth, TIMELINE_HEIGHT);
                timeLineParams.addRule(RelativeLayout.BELOW, R.id.aliyun_preview);
                timeLineParams.topMargin = -timelinePosY;
                mRecordTimelineView.setColor(tintColor, timelineDelBgColor, R.color.qupai_black_opacity_70pct, timelineBgColor);
                break;
            case AliyunSnapVideoParam.RATIO_MODE_9_16:
                previewParams = new RelativeLayout.LayoutParams(screenWidth, screenWidth * 16 / 9);
                if (previewParams.height > screenHeight) {
                    previewParams.height = screenHeight;
                }
                timeLineParams = new RelativeLayout.LayoutParams(screenWidth, TIMELINE_HEIGHT);
                timeLineParams.addRule(RelativeLayout.ABOVE, R.id.aliyun_record_layout);
                timeLineParams.bottomMargin = timelinePosY;
                mRecordTimelineView.setColor(tintColor, timelineDelBgColor, R.color.qupai_black_opacity_70pct, R.color.aliyun_qupai_transparent);
                break;
        }
        if (previewParams != null) {
            mGlSurfaceView.setLayoutParams(previewParams);
        }
        if (timeLineParams != null) {
            mRecordTimelineView.setLayoutParams(timeLineParams);
        }
    }

    private void initOrientationDetector() {
        orientationDetector = new OrientationDetector(getApplicationContext());
    }

    public int getVirtualBarHeight() {
        int vh = 0;
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        try {
            @SuppressWarnings("rawtypes")
            Class c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            vh = dm.heightPixels - windowManager.getDefaultDisplay().getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vh;
    }

    private void initView() {
        mGlSurfaceView = (AliyunSVideoGlSurfaceView) findViewById(R.id.aliyun_preview);
        mGlSurfaceView.setOnTouchListener(this);
        mSwitchRatioBtn = (ImageView) findViewById(R.id.aliyun_switch_ratio);
        mSwitchRatioBtn.setOnClickListener(this);
        mSwitchBeautyBtn = (ImageView) findViewById(R.id.aliyun_switch_beauty);
        mSwitchBeautyBtn.setOnClickListener(this);
        mSwitchCameraBtn = (ImageView) findViewById(R.id.aliyun_switch_camera);
        mSwitchCameraBtn.setOnClickListener(this);
        mSwitchLightBtn = (ImageView) findViewById(R.id.aliyun_switch_light);
        mSwitchLightBtn.setImageResource(lightDisableRes);
        mSwitchLightBtn.setOnClickListener(this);
        mBackBtn = (ImageView) findViewById(R.id.aliyun_back);
        mBackBtn.setOnClickListener(this);
        mRecordBtn = (ImageView) findViewById(R.id.aliyun_record_btn);
        mRecordBtn.setOnTouchListener(this);
        mDeleteBtn = (ImageView) findViewById(R.id.aliyun_delete_btn);
        mDeleteBtn.setOnClickListener(this);
        mCompleteBtn = (ImageView) findViewById(R.id.aliyun_complete_btn);
        mCompleteBtn.setOnClickListener(this);
        mRecordTimelineView = (RecordTimelineView) findViewById(R.id.aliyun_record_timeline);
        mRecordTimelineView.setColor(tintColor, timelineDelBgColor, R.color.qupai_black_opacity_70pct, timelineBgColor);
        mRecordTimeTxt = (TextView) findViewById(R.id.aliyun_record_time);
        mRecordShowtime = (TextView) findViewById(R.id.record_time);
        mRecordLayout = (LinearLayout) findViewById(R.id.ll_record_time);
        mGalleryBtn = (ImageView) findViewById(R.id.aliyun_icon_default);
        circleProgressBar = (CircleProgressView) findViewById(R.id.circle_progress);
        if (!isNeedGallery) {
            mGalleryBtn.setVisibility(View.GONE);
        }
        mToolBar = (FrameLayout) findViewById(R.id.aliyun_tools_bar);
        mFilterTxt = (TextView) findViewById(R.id.aliyun_filter_txt);
        mFilterTxt.setVisibility(View.GONE);
        mGalleryBtn.setOnClickListener(this);
        scaleGestureDetector = new ScaleGestureDetector(this, this);
        gestureDetector = new GestureDetector(this, this);
    }

    private void initSDK() {
        mRecorder = AliyunRecorderCreator.getRecorderInstance(this);
        mRecorder.setDisplayView(mGlSurfaceView);
        mRecorder.setOnFrameCallback(new OnFrameCallBack() {
            @Override
            public void onFrameBack(byte[] bytes, int width, int height, Camera.CameraInfo info) {
                isOpenFailed = false;
            }

            @Override
            public Camera.Size onChoosePreviewSize(List<Camera.Size> supportedPreviewSizes, Camera.Size preferredPreviewSizeForVideo) {
                return null;
            }

            @Override
            public void openFailed() {
                isOpenFailed = true;
            }
        });
        mRecorder.setOnTextureIdCallback(new OnTextureIdCallBack() {
            @Override
            public int onTextureIdBack(int textureId, int textureWidth, int textureHeight, float[] matrix) {
                return textureId;
            }

            @Override
            public int onScaledIdBack(int scaledId, int textureWidth, int textureHeight, float[] matrix) {
                return scaledId;
            }
        });
        mClipManager = mRecorder.getClipManager();
        mClipManager.setMinDuration(minDuration);
        mClipManager.setMaxDuration(maxDuration);
        mRecordTimelineView.setMaxDuration(mClipManager.getMaxDuration());
        mRecordTimelineView.setMinDuration(mClipManager.getMinDuration());
        int[] resolution = getResolution();
        final MediaInfo info = new MediaInfo();
        info.setVideoWidth(resolution[0]);
        info.setVideoHeight(resolution[1]);
        mRecorder.setMediaInfo(info);
        mCameraType = mRecorder.getCameraCount() == 1 ? CameraType.BACK : mCameraType;
        mRecorder.setCamera(mCameraType);
        mRecorder.setGop(gop);
        mRecorder.setVideoBitrate(mBitrate);
        mRecorder.setVideoQuality(videoQuality);
        mRecorder.setRecordCallback(new RecordCallback() {
            @Override
            public void onComplete(boolean validClip, long clipDuration) {
                handleRecordCallback(validClip, clipDuration);
                if (isOnMaxDuration) {
                    isOnMaxDuration = false;
//                    toEditor();
                    mRecorder.finishRecording();
                }
                if (!isNeedClip) {
//                    toEditor();
                    mRecorder.finishRecording();
                }
            }

            @Override
            public void onFinish(String outputPath) {
                scanFile(outputPath);
                mClipManager.deleteAllPart();
                //发送广播
//                Intent intent = new Intent();
//                intent.putExtra(OUTPUT_PATH, outputPath);
//                intent.putExtra(RESULT_TYPE, RESULT_TYPE_RECORD);
//                intent.setAction("take_video");
//                BroadCastManager.getInstance().sendBroadCast(VideoRecorderActivity.this, intent);
//                finish();

                Intent intent = new Intent();
                intent.putExtra(OUTPUT_PATH, outputPath);
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void onProgress(final long duration) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRecordTimelineView.setDuration((int) duration);
                        int time = (int) (mClipManager.getDuration() + duration) / 1000;
                        int min = time / 60;
                        int sec = time % 60;

                        mRecordTimeTxt.setText(String.format("%1$02d:%2$02d", min, sec) + " / 10:00");
                        circleProgressBar.setProgress(1 + time / 6);
                        if (mRecordTimeTxt.getVisibility() != View.VISIBLE) {
                            mRecordTimeTxt.setVisibility(View.VISIBLE);
                            mSwitchCameraBtn.setVisibility(View.GONE);
                            mToolBar.setVisibility(View.GONE);
                            mRecordLayout.setVisibility(View.GONE);
                        }
                    }
                });

            }

            @Override
            public void onMaxDuration() {
                isOnMaxDuration = true;
            }

            @Override
            public void onError(int errorCode) {
                isRecordError = true;
                handleRecordCallback(false, 0);
            }

            @Override
            public void onInitReady() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (filterList != null && filterList.length > filterIndex) {
                            EffectFilter effectFilter = new EffectFilter(filterList[filterIndex]);
                            mRecorder.applyFilter(effectFilter);
                        }
                        if (isBeautyOn) {
                            mRecorder.setBeautyLevel(mBeautyLevel);
                        }
                    }
                });
            }

            @Override
            public void onDrawReady() {

            }

            @Override
            public void onPictureBack(Bitmap bitmap) {

            }

            @Override
            public void onPictureDataBack(byte[] data) {

            }

        });

        setRecordMode(getIntent().getIntExtra(AliyunSnapVideoParam.RECORD_MODE, AliyunSnapVideoParam.RECORD_MODE_AUTO));
        setFilterList(getIntent().getStringArrayExtra(AliyunSnapVideoParam.FILTER_LIST));
        mBeautyLevel = getIntent().getIntExtra(AliyunSnapVideoParam.BEAUTY_LEVEL, 80);
        setBeautyLevel(mBeautyLevel);
        setBeautyStatus(getIntent().getBooleanExtra(AliyunSnapVideoParam.BEAUTY_STATUS, true));
        setCameraType((CameraType) getIntent().getSerializableExtra(AliyunSnapVideoParam.CAMERA_TYPE));
        setFlashType((FlashType) getIntent().getSerializableExtra(AliyunSnapVideoParam.FLASH_TYPE));
        mRecorder.setExposureCompensationRatio(exposureCompensationRatio);
        mRecorder.setFocusMode(CameraParam.FOCUS_MODE_CONTINUE);
    }

    private void scanFile(String path) {
        msc.scanFile(path, "video/mp4");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MyGlSurfaceView", "onResume");
        /**
         * 部分android4.4机型会出现跳转Activity gl为空的问题，如果不需要适配，显示视图代码可以去掉
         */
        mGlSurfaceView.setVisibility(View.VISIBLE);
        mRecorder.startPreview();
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
            mRecorder.cancelRecording();
            isRecording = false;
        }
        mRecorder.stopPreview();

        /**
         * 部分android4.4机型会出现跳转Activity gl为空的问题，如果不需要适配，隐藏视图代码可以去掉
         */
        mGlSurfaceView.setVisibility(View.INVISIBLE);
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

    private void getData() {

        mResolutionMode = getIntent().getIntExtra(AliyunSnapVideoParam.VIDEO_RESOLUTION, AliyunSnapVideoParam.RESOLUTION_540P);
        minDuration = getIntent().getIntExtra(AliyunSnapVideoParam.MIN_DURATION, 2000);
        maxDuration = getIntent().getIntExtra(AliyunSnapVideoParam.MAX_DURATION, 30000);
        mRatioMode = getIntent().getIntExtra(AliyunSnapVideoParam.VIDEO_RATIO, AliyunSnapVideoParam.RATIO_MODE_3_4);
        gop = getIntent().getIntExtra(AliyunSnapVideoParam.VIDEO_GOP, 5);
        mBitrate = getIntent().getIntExtra(AliyunSnapVideoParam.VIDEO_BITRATE, 0);
        videoQuality = (VideoQuality) getIntent().getSerializableExtra(AliyunSnapVideoParam.VIDEO_QUALITY);
        if (videoQuality == null) {
            videoQuality = VideoQuality.HD;
        }
        isNeedClip = getIntent().getBooleanExtra(AliyunSnapVideoParam.NEED_CLIP, true);
        isNeedGallery = getIntent().getBooleanExtra(NEED_GALLERY, true) && galleryVisibility == 0;
        mVideoParam = new AliyunVideoParam.Builder()
                .gop(gop)
                .bitrate(mBitrate)
                .frameRate(25)
                .videoQuality(videoQuality)
                .build();

        /**
         * 裁剪参数
         */
        mFrame = getIntent().getIntExtra(AliyunSnapVideoParam.VIDEO_FRAMERATE, 25);
        mCropMode = (ScaleMode) getIntent().getSerializableExtra(AliyunSnapVideoParam.CROP_MODE);
        if (mCropMode == null) {
            mCropMode = ScaleMode.PS;
        }
        minCropDuration = getIntent().getIntExtra(AliyunSnapVideoParam.MIN_CROP_DURATION, 2000);
        minVideoDuration = getIntent().getIntExtra(AliyunSnapVideoParam.MIN_VIDEO_DURATION, 2000);
        maxVideoDuration = getIntent().getIntExtra(AliyunSnapVideoParam.MAX_VIDEO_DURATION, 10000);
        mSortMode = getIntent().getIntExtra(AliyunSnapVideoParam.SORT_MODE, AliyunSnapVideoParam.SORT_MODE_MERGE);

    }

    public void setVideoParam(int resolutionMode, int minDuration, int maxDuration, int ratioMode, int gop, VideoQuality videoQuality) {
        mResolutionMode = resolutionMode;
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
        mRatioMode = ratioMode;
        this.gop = gop;
        this.videoQuality = videoQuality;
        mVideoParam = new AliyunVideoParam.Builder()
                .gop(gop)
                .frameRate(25)
                .videoQuality(videoQuality)
                .build();
    }

    public void setRecordMode(int recordMode) {
        this.recordMode = recordMode;
    }

    public void setFilterList(String[] filterList) {
        this.filterList = filterList;
    }

    public void setBeautyStatus(boolean on) {
        isBeautyOn = on;
        if (isBeautyOn) {
            mSwitchBeautyBtn.setActivated(true);
        } else {
            mSwitchBeautyBtn.setActivated(false);
        }
        mRecorder.setBeautyStatus(on);
    }

    public void setBeautyLevel(int level) {
        if (isBeautyOn) {
            mRecorder.setBeautyLevel(level);
        }
    }

    public void setCameraType(CameraType cameraType) {
        if (cameraType == null) {
            return;
        }
        mRecorder.setCamera(cameraType);
        mCameraType = cameraType;
        if (mCameraType == CameraType.BACK) {
            mSwitchCameraBtn.setActivated(false);
        } else if (mCameraType == CameraType.FRONT) {
            mSwitchCameraBtn.setActivated(true);
        }
    }

    public void setFlashType(FlashType flashType) {
        if (flashType == null) {
            return;
        }
        if (mCameraType == CameraType.FRONT) {
            mSwitchLightBtn.setEnabled(false);
            mSwitchLightBtn.setImageResource(lightDisableRes);
            return;
        } else if (mCameraType == CameraType.BACK) {
            mSwitchLightBtn.setEnabled(true);
            mSwitchLightBtn.setImageResource(lightSwitchRes);
        }
        mFlashType = flashType;
        switch (mFlashType) {
            case AUTO:
                mSwitchLightBtn.setSelected(false);
                mSwitchLightBtn.setActivated(true);
                break;
            case ON:
                mSwitchLightBtn.setSelected(true);
                mSwitchLightBtn.setActivated(false);
                break;
            case OFF:
                mSwitchLightBtn.setSelected(true);
                mSwitchLightBtn.setActivated(true);
                break;
        }
        mRecorder.setLight(mFlashType);
    }

    private int[] getResolution() {
        int[] resolution = new int[2];
        int width = 0;
        int height = 0;
        switch (mResolutionMode) {
            case AliyunSnapVideoParam.RESOLUTION_360P:
                width = 360;
                break;
            case AliyunSnapVideoParam.RESOLUTION_480P:
                width = 480;
                break;
            case AliyunSnapVideoParam.RESOLUTION_540P:
                width = 540;
                break;
            case AliyunSnapVideoParam.RESOLUTION_720P:
                width = 720;
                break;
        }
        switch (mRatioMode) {
            case AliyunSnapVideoParam.RATIO_MODE_1_1:
                height = width;
                break;
            case AliyunSnapVideoParam.RATIO_MODE_3_4:
                height = width * 4 / 3;
                break;
            case AliyunSnapVideoParam.RATIO_MODE_9_16:
                height = width * 16 / 9;
                break;
        }
        resolution[0] = width;
        resolution[1] = height;
        return resolution;
    }

    public Context getContext() {
        return this;
    }

    @Override
    public void onBackPressed() {
        if (!isRecording) {
            setResult(Activity.RESULT_CANCELED);
            if (!isVideoTake) {
                finish();
            } else {
                if (IsTableUtils.isTablet(getContext())) {
                    List<String> itemStr = new ArrayList<>();
                    itemStr.add("此时退出，照片或视频将会丢失！");
                    itemStr.add("退出");

                    final ListPopupWindow listPopupWindow = new ListPopupWindow(getContext());
                    listPopupWindow.showPopupWindow(mBackBtn, null, itemStr, new ListPopupWindow.ListPopupWindowListener() {
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
        if (mRecorder != null) {
            mRecorder.getClipManager().deleteAllPart();//直接返回则删除所有临时文件
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecorder.destroy();
        msc.disconnect();
        if (orientationDetector != null) {
            orientationDetector.setOrientationChangedListener(null);
        }
        AliyunRecorderCreator.destroyRecorderInstance();
    }

    @Override
    public void onClick(View v) {
        if (v == mSwitchBeautyBtn) {
            if (isBeautyOn) {
                isBeautyOn = false;
                mSwitchBeautyBtn.setActivated(false);
            } else {
                isBeautyOn = true;
                mSwitchBeautyBtn.setActivated(true);
            }
            mRecorder.setBeautyStatus(isBeautyOn);
        } else if (v == mSwitchCameraBtn) {
            int type = mRecorder.switchCamera();
            if (type == CameraType.BACK.getType()) {
                mCameraType = CameraType.BACK;
                mSwitchLightBtn.setEnabled(true);
                mSwitchLightBtn.setImageResource(lightSwitchRes);
                mSwitchCameraBtn.setActivated(false);
                setFlashType(mFlashType);
            } else if (type == CameraType.FRONT.getType()) {
                mCameraType = CameraType.FRONT;
                mSwitchLightBtn.setEnabled(false);
                mSwitchLightBtn.setImageResource(lightDisableRes);
                mSwitchCameraBtn.setActivated(true);
            }
        } else if (v == mSwitchLightBtn) {
            if (mFlashType == FlashType.OFF) {
                mFlashType = FlashType.AUTO;
            } else if (mFlashType == FlashType.AUTO) {
                mFlashType = FlashType.ON;
            } else if (mFlashType == FlashType.ON || mFlashType == FlashType.TORCH) {
                mFlashType = FlashType.OFF;
            }
            switch (mFlashType) {
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
            mRecorder.setLight(mFlashType);
        } else if (v == mSwitchRatioBtn) {
        } else if (v == mBackBtn) {
            onBackPressed();
        } else if (v == mCompleteBtn) {
            mRecorder.finishRecording();
            mClipManager.deleteAllPart();
        } else if (v == mDeleteBtn) {
            if (!isSelected) {
                mRecordTimelineView.selectLast();
                mDeleteBtn.setActivated(true);
                isSelected = true;
            } else {
                mRecordTimelineView.deleteLast();
                mDeleteBtn.setActivated(false);
                mClipManager.deletePart();
                isSelected = false;
                showComplete();
                if (mClipManager.getDuration() == 0) {
                    if (isNeedGallery) {
                        mGalleryBtn.setVisibility(View.VISIBLE);
                    }
                    mSwitchRatioBtn.setEnabled(true);
                    mCompleteBtn.setVisibility(View.GONE);
                    mDeleteBtn.setVisibility(View.GONE);
                }
            }
        } else if (v == mGalleryBtn) {
            Class crop = null;
            try {
                crop = Class.forName("com.aliyun.demo.crop.MediaActivity");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            if (crop == null) {
                Toast.makeText(this, "当前不包含导入模块", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, crop);
            intent.putExtra(AliyunSnapVideoParam.VIDEO_RESOLUTION, mResolutionMode);
            intent.putExtra(AliyunSnapVideoParam.VIDEO_RATIO, mRatioMode);
            intent.putExtra(AliyunSnapVideoParam.NEED_RECORD, false);
            intent.putExtra(AliyunSnapVideoParam.VIDEO_QUALITY, videoQuality);
            intent.putExtra(AliyunSnapVideoParam.VIDEO_GOP, gop);
            intent.putExtra(AliyunSnapVideoParam.VIDEO_BITRATE, mBitrate);
            intent.putExtra(AliyunSnapVideoParam.VIDEO_FRAMERATE, mFrame);
            intent.putExtra(AliyunSnapVideoParam.CROP_MODE, mCropMode);
            intent.putExtra(AliyunSnapVideoParam.MIN_CROP_DURATION, minCropDuration);
            intent.putExtra(AliyunSnapVideoParam.MIN_VIDEO_DURATION, minVideoDuration);
            intent.putExtra(AliyunSnapVideoParam.MAX_VIDEO_DURATION, maxVideoDuration);
            intent.putExtra(AliyunSnapVideoParam.SORT_MODE, mSortMode);
            startActivityForResult(intent, REQUEST_CROP);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CROP && resultCode == RESULT_OK) {
            data.putExtra(RESULT_TYPE, RESULT_TYPE_CROP);
            setResult(Activity.RESULT_OK, data);
            finish();
        }
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
        if (mCameraType == CameraType.FRONT) {
            if (rotation != 0) {
                rotation = 360 - rotation;
            }
        }
        Log.d("MyOrientationDetector", "generated rotation ..." + rotation);
        return rotation;
    }

    private void toEditor() {
        Uri projectUri = mRecorder.finishRecordingForEdit();
        AliyunIClipManager mClipManager = mRecorder.getClipManager();
        List<String> tempFileList = mClipManager.getVideoPathList();
        Class editor = null;
        try {
            editor = Class.forName("com.aliyun.demo.editor.EditorActivity");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (editor == null) {
            toEditor();
            return;
        }
        Intent intent = new Intent(this, editor);
        int[] resolutions = getResolution();
        mVideoParam.setScaleMode(ScaleMode.LB);
        mVideoParam.setOutputWidth(resolutions[0]);
        mVideoParam.setOutputHeight(resolutions[1]);
        intent.putExtra("video_param", mVideoParam);
        intent.putExtra("project_json_path", projectUri.getPath());
        intent.putStringArrayListExtra("temp_file_list", (ArrayList<String>) tempFileList);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            toStitch();
        }
    }

    private void toStitch() {
        mRecorder.finishRecording();
        AliyunIClipManager mClipManager = mRecorder.getClipManager();
        mClipManager.deleteAllPart();//删除所有的临时文件
    }

    private void startRecording() {
        String videoPath = CFConstant.VIDEO_FULL_CACHE_PATH + System.currentTimeMillis() + "_video.mp4";
        mRecorder.setOutputPath(videoPath);
        handleRecordStart();
        mRecorder.setRotation(getPictureRotation());
        isRecordError = false;
        mRecorder.startRecording();
        if (mFlashType == FlashType.ON && mCameraType == CameraType.BACK) {
            mRecorder.setLight(FlashType.TORCH);
        }
    }

    private void stopRecording() {
        mRecorder.stopRecording();
        handleRecordStop();
    }

    private boolean checkIfStartRecording() {
        if (mRecordBtn.isActivated()) {
            return false;
        }
        if (CommonUtil.SDFreeSize() < 50 * 1000 * 1000) {
            Toast.makeText(this, "剩余磁盘空间不足", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void showFilter(String name) {
        if (name == null || name.isEmpty()) {
            name = "原片";
        }
        mFilterTxt.animate().cancel();
        mFilterTxt.setText(name);
        mFilterTxt.setVisibility(View.VISIBLE);
        mFilterTxt.setAlpha(FADE_IN_START_ALPHA);
        txtFadeIn();
    }

    private void txtFadeIn() {
        mFilterTxt.animate().alpha(1).setDuration(FILTER_ANIMATION_DURATION / 2).setListener(
                new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        txtFadeOut();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    }

    private void txtFadeOut() {
        mFilterTxt.animate().alpha(0).setDuration(FILTER_ANIMATION_DURATION / 2).start();
        mFilterTxt.animate().setListener(null);
    }

    private boolean isVideoTake;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == mRecordBtn) {
            if (isOpenFailed) {
                Toast.makeText(this, "请开启摄像头权限", Toast.LENGTH_SHORT).show();
                return true;
            }
            isVideoTake = true;
            if (recordMode == AliyunSnapVideoParam.RECORD_MODE_TOUCH) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!isRecording) {
                        if (!checkIfStartRecording()) {
                            return false;
                        }
                        mRecordBtn.setHovered(true);
                        startRecording();
                        isRecording = true;
                    } else {
                        stopRecording();
                        isRecording = false;
                    }
                }
            } else if (recordMode == AliyunSnapVideoParam.RECORD_MODE_PRESS) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!checkIfStartRecording()) {
                        return false;
                    }
                    mRecordBtn.setSelected(true);
                    startRecording();
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    stopRecording();
                }
            } else if (recordMode == AliyunSnapVideoParam.RECORD_MODE_AUTO) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    downTime = System.currentTimeMillis();
                    if (!isRecording) {
                        if (!checkIfStartRecording()) {
                            return false;
                        }
                        mRecordBtn.setPressed(true);
                        startRecording();
                        mRecordBtn.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (mRecordBtn.isPressed()) {
                                    mRecordBtn.setSelected(true);
                                    mRecordBtn.setHovered(false);
                                }
                            }
                        }, 200);
                        isRecording = true;
                    } else {
                        stopRecording();
                        isRecording = false;
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    long timeOffset = System.currentTimeMillis() - downTime;
                    mRecordBtn.setPressed(false);
                    if (timeOffset > 1000) {
                        stopRecording();
                        isRecording = false;
                    } else {
                        if (!isRecordError) {
                            mRecordBtn.setSelected(false);
                            mRecordBtn.setHovered(true);
                        } else {
                            isRecording = false;
                        }
                    }
                }
            }
        } else if (v.equals(mGlSurfaceView)) {
            if (event.getPointerCount() >= 2) {
                scaleGestureDetector.onTouchEvent(event);
            } else if (event.getPointerCount() == 1) {
                gestureDetector.onTouchEvent(event);
            }


        }
        return true;
    }

    private void handleRecordStart() {
        mRecordBtn.setActivated(true);
        mGalleryBtn.setVisibility(View.GONE);
        mCompleteBtn.setVisibility(View.VISIBLE);
        mDeleteBtn.setVisibility(View.VISIBLE);
        mSwitchRatioBtn.setEnabled(false);
        mSwitchBeautyBtn.setEnabled(false);
        mSwitchCameraBtn.setEnabled(false);
        mSwitchLightBtn.setEnabled(false);
        mCompleteBtn.setEnabled(false);
        mDeleteBtn.setEnabled(false);
        mDeleteBtn.setActivated(false);
        isSelected = false;
    }

    private void handleRecordStop() {
        if (mFlashType == FlashType.ON && mCameraType == CameraType.BACK) {
            mRecorder.setLight(FlashType.OFF);
        }
    }


    private void showComplete() {
        if (mClipManager.getDuration() > mClipManager.getMinDuration()) {
            mCompleteBtn.setActivated(true);
        } else {
            mCompleteBtn.setActivated(false);
        }
    }

    private void handleRecordCallback(final boolean validClip, final long clipDuration) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRecordBtn.setActivated(false);
                mRecordBtn.setHovered(false);
                mRecordBtn.setSelected(false);
                if (validClip) {
                    mRecordTimelineView.setDuration((int) clipDuration);
                    mRecordTimelineView.clipComplete();
                } else {
                    mRecordTimelineView.setDuration(0);
                }
                Log.e("validClip", "validClip : " + validClip);
                mRecordTimeTxt.setVisibility(View.GONE);
                mSwitchCameraBtn.setVisibility(View.VISIBLE);
                mToolBar.setVisibility(View.VISIBLE);
                mRecordLayout.setVisibility(View.VISIBLE);
                mRecordShowtime.setText(mRecordTimeTxt.getText().toString());
                mSwitchBeautyBtn.setEnabled(true);
                mSwitchCameraBtn.setEnabled(true);
                mSwitchLightBtn.setEnabled(true);
                mCompleteBtn.setEnabled(true);
                mDeleteBtn.setEnabled(true);
                showComplete();
            }
        });

    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float factorOffset = detector.getScaleFactor() - lastScaleFactor;
        scaleFactor += factorOffset;
        lastScaleFactor = detector.getScaleFactor();
        if (scaleFactor < 0) {
            scaleFactor = 0;
        }
        if (scaleFactor > 1) {
            scaleFactor = 1;
        }
        mRecorder.setZoom(scaleFactor);
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

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    int mTestFocusIndex;

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();

//        float[] testpoint = new float[] {0, 0,
//                                         1, 0,
//                                         0, 1,
//                                         1, 1,
//                                         0.5f, 0.5f,
//                                         0.1f, 0.1f,
//                                         0.5f, 0.8f};
//        int index = mTestFocusIndex % 7 * 2;
        mRecorder.setFocus(x / mGlSurfaceView.getWidth(), y / mGlSurfaceView.getHeight());
//        mTestFocusIndex++;
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (Math.abs(distanceX) > 20) {
            return false;
        }
        exposureCompensationRatio += (distanceY / mGlSurfaceView.getHeight());
        if (exposureCompensationRatio > 1) {
            exposureCompensationRatio = 1;
        }
        if (exposureCompensationRatio < 0) {
            exposureCompensationRatio = 0;
        }
        mRecorder.setExposureCompensationRatio(exposureCompensationRatio);
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (filterList == null || filterList.length == 0) {
            return true;
        }
        if (mRecordBtn.isActivated()) {
            return true;
        }
        if (velocityX > MAX_SWITCH_VELOCITY) {
            filterIndex++;
            if (filterIndex >= filterList.length) {
                filterIndex = 0;
            }
        } else if (velocityX < -MAX_SWITCH_VELOCITY) {
            filterIndex--;
            if (filterIndex < 0) {
                filterIndex = filterList.length - 1;
            }
        } else {
            return true;
        }
        EffectFilter effectFilter = new EffectFilter(filterList[filterIndex]);
        mRecorder.applyFilter(effectFilter);
        showFilter(effectFilter.getName());
        return false;
    }
}
