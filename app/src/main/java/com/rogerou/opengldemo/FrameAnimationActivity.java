package com.rogerou.opengldemo;

import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.rogerou.opengldemo.filter.MyFrameAnimationFilter;
import com.rogerou.opengldemo.render.FrameRender;

import static android.opengl.GLSurfaceView.RENDERMODE_WHEN_DIRTY;

/**
 * Created by Administrator on 2017/2/11.
 */

public class FrameAnimationActivity extends AppCompatActivity implements MyFrameAnimationFilter.MyCallBack {


    private GLSurfaceView mGLSurfaceView;

    private MyFrameAnimationFilter mMyFrameAnimationFilter;
    private String mAnimation = "assets/etczip/cc.zip";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView.setZOrderOnTop(true);
        mGLSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mMyFrameAnimationFilter = new MyFrameAnimationFilter(getAssets(), this);
        mGLSurfaceView.setRenderer(new FrameRender(mMyFrameAnimationFilter));
        mGLSurfaceView.setRenderMode(RENDERMODE_WHEN_DIRTY);
        setContentView(mGLSurfaceView);
        mMyFrameAnimationFilter.setAnimation(mAnimation, 50);
        mMyFrameAnimationFilter.start();
    }

    @Override
    public void requestRender() {
        mGLSurfaceView.requestRender();
    }

    @Override
    public void onStateChange() {
        if (!mMyFrameAnimationFilter.isPlay()) {
            mMyFrameAnimationFilter.setAnimation(mAnimation, 50);
            mMyFrameAnimationFilter.start();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        mMyFrameAnimationFilter.stop();
    }
}
