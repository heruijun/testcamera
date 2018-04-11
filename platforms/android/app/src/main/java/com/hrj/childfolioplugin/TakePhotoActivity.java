package com.hrj.childfolioplugin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.my.testcamera.R;

public class TakePhotoActivity extends Activity {

    private TextView mFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.take_camera);
        mFinish = findViewById(R.id.finish);
        mFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishRecording();
            }
        });
    }

    private void finishRecording() {
        //返回数据
        Intent intent = new Intent();
        intent.putExtra("data", "aaa");
        setResult(RESULT_OK, intent);
        finish();
    }
}
