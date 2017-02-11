package com.rogerou.opengldemo;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Administrator on 2017/2/10.
 */

public abstract class BaseShaderActivity extends AppCompatActivity implements GLSurfaceView.Renderer {


    private GLSurfaceView mGLSurfaceView;

    protected int mProgram;
    protected int mPositionHandle;

    protected FloatBuffer mFloatBuffer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setRenderer(this);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        setContentView(mGLSurfaceView);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        setup();
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glUseProgram(mProgram);
        draw();
    }


    protected void initVertexBuffer() {
        mFloatBuffer = ByteBuffer.allocateDirect(getCoords().length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(getCoords());
        mFloatBuffer.position(0);
    }

    protected void setup() {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        initVertexBuffer();
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, getVertexShader());
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, getFragmentShader());
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
    }


    public abstract void draw();

    public abstract float[] getCoords();

    public abstract String getVertexShader();

    public abstract String getFragmentShader();

    public abstract float[] getColor();


    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

}
