/*
 *
 * ZipAniView.java
 * 
 * Created by Wuwang on 2016/12/8
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.rogerou.opengldemo.glsurfaceview;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.rogerou.opengldemo.filter.MyFrameAnimationFilter;
import com.rogerou.opengldemo.filter.StateChangeListener;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import jp.co.cyberagent.android.gpuimage.OpenGlUtils;

/**
 * Description:
 */
public class MyAnimationView extends GLSurfaceView implements GLSurfaceView.Renderer {


    private MyFrameAnimationFilter mAnimationFilter;

    private FloatBuffer mTextureBuffer;
    private FloatBuffer mVertexBuffer;

    //顶点坐标
    private float mVertext[] = {
            -1.0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, 1.0f,
            1.0f, -1.0f,
    };

    //纹理坐标
    private float[] mTexture = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
    };

    protected void initBuffer() {
        ByteBuffer a = ByteBuffer.allocateDirect(32);
        a.order(ByteOrder.nativeOrder());
        mVertexBuffer = a.asFloatBuffer();
        mVertexBuffer.put(mVertext);
        mVertexBuffer.position(0);
        ByteBuffer b = ByteBuffer.allocateDirect(32);
        b.order(ByteOrder.nativeOrder());
        mTextureBuffer = b.asFloatBuffer();
        mTextureBuffer.put(mTexture);
        mTextureBuffer.position(0);
    }

    public MyAnimationView(Context context) {
        this(context, null);
    }

    public MyAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initBuffer();
        init();
    }

    private void init() {
        setEGLContextClientVersion(2);
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        mAnimationFilter = new MyFrameAnimationFilter(getResources().getAssets());
    }


    public void setAnimation(String path, int timeStep) {
        mAnimationFilter.setAnimation(this, path, timeStep);
    }

    public void start() {
        mAnimationFilter.start();
    }

    public void stop() {
        mAnimationFilter.stop();
    }

    public boolean isPlay() {
        return mAnimationFilter.isPlay();
    }

    public void setStateChangeListener(StateChangeListener listener) {
        mAnimationFilter.setStateChangeListener(listener);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mAnimationFilter.init();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mAnimationFilter.onOutputSizeChanged(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mAnimationFilter.onDraw(OpenGlUtils.NO_TEXTURE, mVertexBuffer, mTextureBuffer);
    }

}
