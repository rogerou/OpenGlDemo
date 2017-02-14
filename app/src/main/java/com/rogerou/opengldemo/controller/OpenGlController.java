package com.rogerou.opengldemo.controller;

import android.content.Context;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;

import com.rogerou.opengldemo.filter.GroupFilter;
import com.rogerou.opengldemo.filter.MyImagefilter;
import com.rogerou.opengldemo.render.MyRender;

import java.util.ArrayList;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.Rotation;

/**
 * Created by Administrator on 2017/2/10.
 */

public class OpenGlController {


    private final MyRender mMyRender;
    private GPUImageFilter mGPUImageFilter;
    private GLSurfaceView mGLSurfaceView1;
    private final Context mContext;

    public OpenGlController(Context context) {
        mContext = context;
        mGPUImageFilter = new GPUImageFilter();
        mMyRender = new MyRender(mGPUImageFilter);
    }

    public void setFilter(GPUImageFilter gpuImageFilter) {
        this.mGPUImageFilter = gpuImageFilter;
        mMyRender.setFilter(mGPUImageFilter);
        mGLSurfaceView1.requestRender();
    }


    public void setGLSurfaceView1(GLSurfaceView glSurfaceView) {
        mGLSurfaceView1 = glSurfaceView;
        mGLSurfaceView1.setEGLContextClientVersion(2);
        mGLSurfaceView1.setRenderer(mMyRender);
        mGLSurfaceView1.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    /**
     * 初始化Camera
     */
    public void setUpCamera(final Camera camera, final int degrees, final boolean flipHorizontal,
                            final boolean flipVertical) {
        //开启不断渲染模式
        mGLSurfaceView1.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        mMyRender.setUpSurfaceTexture(camera);
        Rotation rotation = Rotation.NORMAL;
        switch (degrees) {
            case 90:
                rotation = Rotation.ROTATION_90;
                break;
            case 180:
                rotation = Rotation.ROTATION_180;
                break;
            case 270:
                rotation = Rotation.ROTATION_270;
                break;
        }
        mMyRender.setRotation(rotation, flipHorizontal, flipVertical);
    }

    public GPUImageFilter getGPUImageFilter() {
        return mGPUImageFilter;
    }
}
