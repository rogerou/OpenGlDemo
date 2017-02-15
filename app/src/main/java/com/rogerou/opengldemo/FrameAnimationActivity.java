package com.rogerou.opengldemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.rogerou.opengldemo.util.StateChangeListener;
import com.rogerou.opengldemo.glsurfaceview.MyAnimationView;

/**
 * Created by Administrator on 2017/2/11.
 */

public class FrameAnimationActivity extends AppCompatActivity {


    private MyAnimationView mGLSurfaceView;
    private String mAnimation = "assets/cc.zip";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frame);
        mGLSurfaceView = (MyAnimationView) findViewById(R.id.gl_view);
        mGLSurfaceView.setOnClickListener(v -> {
            if (!mGLSurfaceView.isPlay()) {
                mGLSurfaceView.setAnimation(mAnimation, 50);
                mGLSurfaceView.start();
            }
        });

        mGLSurfaceView.setStateChangeListener((lastState, nowState) -> {
            if (nowState == StateChangeListener.STOP) {
                if (!mGLSurfaceView.isPlay()) {
                    mGLSurfaceView.setAnimation(mAnimation, 50);
                    mGLSurfaceView.start();
                }
            }
        });

    }


}
