package com.rogerou.opengldemo.GlsurfaceView;

import android.content.Context;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

import static com.rogerou.opengldemo.Constant.CUBE;
import static com.rogerou.opengldemo.Constant.TEXTURE_NO_ROTATION;

/**
 * Created by Administrator on 2017/2/9.
 */

public class MyGLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer {

    protected GPUImageFilter mGPUImageFilter;
    /**
     * 顶点坐标
     */
    protected final FloatBuffer mGLCubeBuffer;

    /**
     * 纹理坐标
     */
    protected final FloatBuffer mGLTextureBuffer;

    /**
     * GLSurfaceView的宽高
     */
    protected int mSurfaceWidth, mSurfaceHeight;

    /**
     * 图像宽高
     */
    protected int mImageWidth, mImageHeight;


    public MyGLSurfaceView(Context context) {
        this(context, null);
    }

    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        setRenderer(this);
        mGLCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(CUBE);
        mGLCubeBuffer.position(0);

        mGLTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(TEXTURE_NO_ROTATION);
        mGLTextureBuffer.position(0);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {


    }


    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }


    public void setFilter(GPUImageFilter GPUImageFilter) {
        queueEvent(() -> {
            if (mGPUImageFilter != null) {
                mGPUImageFilter.destroy();
                mGPUImageFilter = null;
            }
            mGPUImageFilter = GPUImageFilter;
            mGPUImageFilter.init();
        });
        requestRender();
    }

    /**
     * 重置大小
     */
    protected void onFilterChanged() {
        if (mGPUImageFilter != null) {
//            mGPUImageFilter.onDisplaySizeChanged(mSurfaceWidth, mSurfaceHeight);
//            mGPUImageFilter.onInputSizeChanged(mImageWidth, mImageHeight);
        }
    }


    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glDisable(GL10.GL_DITHER);
        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glEnable(GL10.GL_CULL_FACE);
        GLES20.glEnable(GL10.GL_DEPTH_TEST);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        onFilterChanged();
    }


    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);


    }


}
