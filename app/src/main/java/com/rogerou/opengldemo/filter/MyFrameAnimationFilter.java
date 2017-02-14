/*
 *
 * ZipMulDrawer.java
 * 
 * Created by Wuwang on 2016/12/8
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.rogerou.opengldemo.filter;

import android.content.res.AssetManager;
import android.opengl.ETC1;
import android.opengl.ETC1Util;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.rogerou.opengldemo.util.ZipPkmReader;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.OpenGlUtils;


public class MyFrameAnimationFilter extends GPUImageFilter {


    public static final String FRAGMENT_SHADER = "precision mediump float;\n" +
            "   varying vec2 aCoord;\n" +
            "   uniform sampler2D vTexture;\n" +
            "   uniform sampler2D vTextureAlpha;\n" +
            "   \n" +
            "   void main() {\n" +
            "       vec4 color=texture2D( vTexture, aCoord);\n" +
            "       color.a=texture2D(vTextureAlpha,aCoord).r;\n" +
            "       gl_FragColor = color;\n" +
            "   }";

    public static final String VERTEX_SHADER = "attribute vec4 vPosition;\n" +
            "   attribute vec2 vCoord;\n" +
            "   varying vec2 aCoord;\n" +
            "   uniform mat4 vMatrix;\n" +
            "   \n" +
            "   void main(){\n" +
            "       aCoord = vCoord;\n" +
            "       gl_Position = vMatrix*vPosition;\n" +
            "   }";
    private GLSurfaceView mView;
    private int mTimeStep = 50;
    private boolean isPlay = false;
    private ByteBuffer mByteBuffer;

    private int[] mTexture;

    private ZipPkmReader mPkmReader;
    private int mGlHAlpha;

    private StateChangeListener mStateChangeListener;
    private int mHPosition;
    private int mHCoord;
    private int mHMatrix;
    private int mHTexture;
    private float[] matrix = {
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1};

    public MyFrameAnimationFilter(AssetManager mRes) {
        mPkmReader = new ZipPkmReader(mRes);
    }


    @Override
    public void onInit() {
        mGLProgId = OpenGlUtils.loadProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        mHPosition = GLES20.glGetAttribLocation(mGLProgId, "vPosition");
        mHCoord = GLES20.glGetAttribLocation(mGLProgId, "vCoord");
        mHMatrix = GLES20.glGetUniformLocation(mGLProgId, "vMatrix");
        mHTexture = GLES20.glGetUniformLocation(mGLProgId, "vTexture");
        mTexture = new int[2];
        //创建两个纹理，一个是用来展示图片，一个用来展示透明通道
        createEtcTexture(mTexture);
        mGlHAlpha = GLES20.glGetUniformLocation(mGLProgId, "vTextureAlpha");
    }


    @Override
    public void onOutputSizeChanged(int width, int height) {
        super.onOutputSizeChanged(width, height);
        mByteBuffer = ByteBuffer.allocateDirect(ETC1.getEncodedDataSize(width, height));
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

    }

    //绘制并绑定纹理
    private void BindTexture() {
        ETC1Util.ETC1Texture texture = mPkmReader.getNextTexture();
        ETC1Util.ETC1Texture aTexture = mPkmReader.getNextTexture();
        //当为空时当前播放已经完毕，即置一个没有数据的空纹理渲染到屏幕并停止播放

        if (texture == null || aTexture == null) {
            texture = new ETC1Util.ETC1Texture(mOutputWidth, mOutputHeight, mByteBuffer);
            aTexture = new ETC1Util.ETC1Texture(mOutputWidth, mOutputHeight, mByteBuffer);
            isPlay = false;
        }
        for (int i = 0; i < 2; i++) {
            //选择当前激活的纹理
            GLES20.glActiveTexture(i == 0 ? GLES20.GL_TEXTURE0 : GLES20.GL_TEXTURE1);
            //绑定纹理id
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexture[i]);
            //加载纹理
            ETC1Util.loadTexture(GLES20.GL_TEXTURE_2D, 0, 0, GLES20.GL_RGB, GLES20.GL_UNSIGNED_SHORT_5_6_5, i == 0 ? texture : aTexture);
            //将纹理设置给Shader
            GLES20.glUniform1i(i == 0 ? mHTexture : mGlHAlpha, i);
        }
    }

    @Override
    public void onDraw(int textureId, FloatBuffer cubeBuffer, FloatBuffer textureBuffer) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glUseProgram(mGLProgId);
        if (time != 0) {
            Log.e("Frame", "time-->" + (System.currentTimeMillis() - time));
        }
        time = System.currentTimeMillis();
        long startTime = System.currentTimeMillis();
        GLES20.glUniformMatrix4fv(mHMatrix, 1, false, matrix, 0);
        BindTexture();
        drawVertex(cubeBuffer, textureBuffer);
        long s = System.currentTimeMillis() - startTime;
        //这里做了个时间的同步，
        // 如果渲染时间不超过50ms的情况下，
        // 强行让线程休眠到对应的时间再执行，
        // 保持动画的相对同步
        //可能还需要后续的处理，如超过下个50ms的情况下直接跳过此帧
        if (isPlay) {
            if (s < mTimeStep) {
                try {
                    Thread.sleep(mTimeStep - s);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            mView.requestRender();
        } else {
            changeState(StateChangeListener.PLAYING, StateChangeListener.STOP);
        }

    }

    //进行纹理坐标和顶点坐标进行绘制
    private void drawVertex(FloatBuffer cubeBuffer, FloatBuffer textureBuffer) {
        GLES20.glEnableVertexAttribArray(mHPosition);
        GLES20.glVertexAttribPointer(mHPosition, 2, GLES20.GL_FLOAT, false, 0, cubeBuffer);
        GLES20.glEnableVertexAttribArray(mHCoord);
        GLES20.glVertexAttribPointer(mHCoord, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(mHPosition);
        GLES20.glDisableVertexAttribArray(mHCoord);
    }


    private long time = 0;

    public void setAnimation(GLSurfaceView view, String path, int timeStep) {
        this.mView = view;
        this.mTimeStep = timeStep;
        mPkmReader.setZipPath(path);
    }

    public void start() {
        if (!isPlay) {
            stop();
            isPlay = true;
            changeState(StateChangeListener.STOP, StateChangeListener.START);
            mPkmReader.open();
            mView.requestRender();
        }
    }

    public boolean isPlay() {
        return isPlay;
    }

    public void stop() {
        if (mPkmReader != null) {
            mPkmReader.close();
        }
        isPlay = false;
    }

    public void setStateChangeListener(StateChangeListener listener) {
        this.mStateChangeListener = listener;
    }

    private void changeState(int lastState, int nowState) {
        if (this.mStateChangeListener != null) {
            this.mStateChangeListener.onStateChanged(lastState, nowState);
        }
    }

    private void createEtcTexture(int[] texture) {
        //生成纹理
        GLES20.glGenTextures(2, texture, 0);
        for (int i = 0; i < texture.length; i++) {
            //绑定纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[i]);
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            //根据以上指定的参数，生成一个2D纹理
        }
    }


}
