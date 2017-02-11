package com.rogerou.opengldemo.render;

import android.opengl.GLSurfaceView;

import com.rogerou.opengldemo.Constant;
import com.rogerou.opengldemo.filter.MyFrameAnimationFilter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.OpenGlUtils;

/**
 * Created by Administrator on 2017/2/11.
 */

public class FrameRender implements GLSurfaceView.Renderer {

    private GPUImageFilter mGPUImageFilter;

    private final FloatBuffer mVertexBuffer;
    private final FloatBuffer mCuberBuffer;

    public FrameRender(GPUImageFilter gpuImageFilter) {
        mGPUImageFilter = gpuImageFilter;
        mVertexBuffer = ByteBuffer.allocateDirect(Constant.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(Constant.TEXTURE_NO_ROTATION);
        mVertexBuffer.position(0);
        mCuberBuffer = ByteBuffer.allocateDirect(Constant.CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mCuberBuffer.position(0);
    }

    public void setGPUImageFilter(GPUImageFilter GPUImageFilter) {
        if (mGPUImageFilter != null) {
            mGPUImageFilter.destroy();
        }
        mGPUImageFilter = GPUImageFilter;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mGPUImageFilter.init();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mGPUImageFilter.onOutputSizeChanged(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mGPUImageFilter.onDraw(OpenGlUtils.NO_TEXTURE, mCuberBuffer, mVertexBuffer);
    }

}
