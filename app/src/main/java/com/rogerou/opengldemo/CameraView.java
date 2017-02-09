package com.rogerou.opengldemo;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Administrator on 2017/2/9.
 */

public class CameraView extends GLSurfaceView implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
    private Camera mCamera;
    private SurfaceTexture mSurfaceTexture;

    private FloatBuffer mFloatBuffer;

    private int mProgram;

    private int mPositionHandle;
    private static final String FRAGMENT_SHADER = "precision mediump float;\n" +
            "varying vec2 v_texCoord;\n" +
            "uniform samplerExternalOES s_texture;\n" +
            "\n" +
            "void main() \n" +
            "  gl_FragColor = texture2D( s_texture, v_texCoord )\n" +
            "}";
    private static final float[] VERTEX = {
            -1.0f, 1.0f, 0.0f,
            -1.0f, -1.0f, 0.0f,
            1.0f, -1.0f, 0.0f,
            1.0f, 1.0f, 0.0f,
    };
    private static final float[] UV_TEX_VERTEX = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f,
    };

    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        mFloatBuffer = ByteBuffer.allocateDirect(VERTEX.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(VERTEX);
        mFloatBuffer.position(0);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mSurfaceTexture = new SurfaceTexture(0);
        mSurfaceTexture.setOnFrameAvailableListener(this);

    }

    private void openCamera() {
        try {
            mCamera = getCameraInstance();
            mCamera.setPreviewTexture(mSurfaceTexture);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        openCamera();
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

    public void releaseCamera() {
        mCamera.release();
        mCamera = null;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        requestRender();
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        mSurfaceTexture = new SurfaceTexture(0);
        mSurfaceTexture.setOnFrameAvailableListener(this);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        openCamera();
    }


    @Override
    public void onDrawFrame(GL10 gl10) {
        if (mSurfaceTexture != null)
            mSurfaceTexture.updateTexImage();
    }


}
