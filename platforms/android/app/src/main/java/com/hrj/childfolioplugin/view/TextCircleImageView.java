package com.hrj.childfolioplugin.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.hrj.childfolioplugin.utils.DipUtils;
import com.my.testcamera.R;

public class TextCircleImageView extends FrameLayout {

    public SimpleDraweeView mDraweeView;
    public TextView mTextView;

    private Context mContext;
    private static final int width = 150;
    private static final int height = 150;

    public TextCircleImageView(Context context) {
        super(context);
        this.mContext = context;
        initView();
    }

    public TextCircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initView();
    }

    public TextCircleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initView();
    }

    private void initView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_text_circle_image, this);
        mDraweeView = view.findViewById(R.id.sdv_head_image);
        mTextView = view.findViewById(R.id.tv_head_text);
    }

    public void setImageUrl(@Nullable String uri) {
//        Glide.with(getContext()).load(uri).transform(new GlideCircleTransform(getContext())).into(mDraweeView);

        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(uri))
                .setResizeOptions(new ResizeOptions(width, height))
                .build();
        PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                .setOldController(mDraweeView.getController())
                .setImageRequest(request)
                .build();
        mDraweeView.setController(controller);

        mTextView.setText("");
    }

    public void setImageBitmap(@Nullable Bitmap bitmap) {
        mDraweeView.setImageBitmap(bitmap);
        mTextView.setText("");
    }

    public void setHeadText(String text) {
        mDraweeView.setImageURI("");
        mTextView.setText(text);
        mTextView.setTextSize(12);
    }

    public SimpleDraweeView getHeader() {
        return mDraweeView;
    }

    public void setHeadText(String first, String last) {
        mDraweeView.setImageURI("");

        String text = "";
        if (!TextUtils.isEmpty(first) && !TextUtils.isEmpty(last)) {
            text = first.substring(0, 1) + last.substring(0, 1);
        }
        if (TextUtils.isEmpty(first) && !TextUtils.isEmpty(last)) {
            text = last.substring(0, 1);
        }
        if (!TextUtils.isEmpty(first) && TextUtils.isEmpty(last)) {
            text = first.substring(0, 1);
        }
        mTextView.setText(text);
    }

    public void setSelected(boolean selected) {
        if (selected) {
            RoundingParams roundingParams = RoundingParams.asCircle();
            roundingParams.setBorder(getResources().getColor(R.color.blue_color), DipUtils.dp2px(getContext(), 4));
            mDraweeView.getHierarchy().setRoundingParams(roundingParams);
        } else {
            RoundingParams roundingParams = RoundingParams.asCircle();
            roundingParams.setBorder(getResources().getColor(R.color.divider_line_color), DipUtils.dp2px(getContext(), 1));
            mDraweeView.getHierarchy().setRoundingParams(roundingParams);
        }
    }

    public class GlideCircleTransform extends BitmapTransformation {
        public GlideCircleTransform(Context context) {
            super(context);
        }

        @Override protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            return circleCrop(pool, toTransform);
        }

        private Bitmap circleCrop(BitmapPool pool, Bitmap source) {
            if (source == null) return null;

            int size = Math.min(source.getWidth(), source.getHeight());
            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            // TODO this could be acquired from the pool too
            Bitmap squared = Bitmap.createBitmap(source, x, y, size, size);

            Bitmap result = pool.get(size, size, Bitmap.Config.ARGB_8888);
            if (result == null) {
                result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(result);
            Paint paint = new Paint();
            paint.setShader(new BitmapShader(squared, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
            paint.setAntiAlias(true);
            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);
            return result;
        }

        @Override public String getId() {
            return getClass().getName();
        }
    }
}