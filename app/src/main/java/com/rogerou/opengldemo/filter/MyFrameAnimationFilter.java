package com.rogerou.opengldemo.filter;

import android.content.res.AssetManager;
import android.opengl.ETC1;
import android.opengl.ETC1Util;
import android.opengl.GLES20;
import android.util.Log;

import com.rogerou.opengldemo.util.ZipPkmReader;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

/**
 * Created by Administrator on 2017/2/11.
 */

public class MyFrameAnimationFilter extends GPUImageFilter {

    private static final String FRAGMENT_SHADER = "precision mediump float;" +
            "varying highp vec2 textureCoordinate;\n" +
            " \n" +
            " uniform sampler2D inputImageTexture;\n" +
            "uniform sampler2D vTextureAlpha; \n" +
            " void main()\n" +
            " {\n" +
            "     lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "      textureColor.a = texture2D(vTextureAlpha,textureCoordinate)  \n" +
            "     gl_FragColor = textureColor;\n" +
            " }";

    private int mGlHAlpha;

    public int TIME_STEP = 50;
    private int[] textureId = new int[2];


    private ZipPkmReader mPkmReader;
    private ByteBuffer mByteBuffer;
    private boolean isPlay;

    private MyCallBack mMyCallBack;

    public MyFrameAnimationFilter(AssetManager manager, MyCallBack myCallBack) {
        super(NO_FILTER_VERTEX_SHADER, FRAGMENT_SHADER);
        mPkmReader = new ZipPkmReader(manager);
        this.mMyCallBack = myCallBack;
    }

    public void setPlay(boolean play) {
        isPlay = play;
    }


    @Override
    public void onOutputSizeChanged(int width, int height) {
        mByteBuffer = ByteBuffer.allocateDirect(ETC1.getEncodedDataSize(width, height));
        super.onOutputSizeChanged(width, height);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }


    @Override
    public void onInit() {
        createEtcTexture(textureId);
        super.onInit();
        mGlHAlpha = GLES20.glGetUniformLocation(mGLProgId, "vTextureAlpha");
        runOnDraw(() -> {


        });
    }


    @Override
    protected void onDrawArraysPre() {
        super.onDrawArraysPre();
        ETC1Util.ETC1Texture texture = mPkmReader.getNextTexture();
        ETC1Util.ETC1Texture aTexture = mPkmReader.getNextTexture();
        if (texture == null || aTexture == null) {
            texture = new ETC1Util.ETC1Texture(mOutputWidth, mOutputHeight, mByteBuffer);
            aTexture = new ETC1Util.ETC1Texture(mOutputWidth, mOutputHeight, mByteBuffer);
            setPlay(false);
        }
        for (int i = 0; i < 2; i++) {
            //选择当前激活的纹理
            GLES20.glActiveTexture(i == 0 ? GLES20.GL_TEXTURE0 : GLES20.GL_TEXTURE1);
            //绑定纹理id
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[i]);
            //加载纹理
            ETC1Util.loadTexture(GLES20.GL_TEXTURE_2D, 0, 0, GLES20.GL_RGB, GLES20.GL_UNSIGNED_SHORT_5_6_5, i == 0 ? texture : aTexture);
            //将纹理设置给Shader
            GLES20.glUniform1i(i == 0 ? mGLUniformTexture : mGlHAlpha, i);
        }


    }

    @Override
    public void onDraw(int textureId, FloatBuffer cubeBuffer, FloatBuffer textureBuffer) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        long startTime = System.currentTimeMillis();
        super.onDraw(textureId, cubeBuffer, textureBuffer);
        long timeMinus = System.currentTimeMillis() - startTime;
        Log.e("Frame", "onDraw: " + timeMinus);
        if (isPlay) {
            if (timeMinus < TIME_STEP) {
                try {
                    Thread.sleep(TIME_STEP - timeMinus);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            mMyCallBack.requestRender();
        } else {
            mMyCallBack.onStateChange();
        }

    }

    //设置动画
    public void setAnimation(String path, int timeStep) {
        mPkmReader.setZipPath(path);
        this.TIME_STEP = timeStep;
    }


    //开始播放
    public void start() {
        if (!isPlay) {
            stop();
            isPlay = true;
            mMyCallBack.onStateChange();
            mPkmReader.open();
            mMyCallBack.requestRender();
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

    private void createEtcTexture(int[] texture) {
        //生成纹理
        GLES20.glGenTextures(2, texture, 0);
        for (int aTexture : texture) {
            //绑定纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, aTexture);
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

    public interface MyCallBack {
        void requestRender();

        void onStateChange();
    }

}
