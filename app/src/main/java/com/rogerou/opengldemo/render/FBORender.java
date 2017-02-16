package com.rogerou.opengldemo.render;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

import static com.rogerou.opengldemo.Constant.CUBE;
import static jp.co.cyberagent.android.gpuimage.util.TextureRotationUtil.TEXTURE_NO_ROTATION;

/**
 * Created by Administrator on 2017/2/11.
 * 离屏渲染的Render
 * 传入Bitmap后缓冲到FrameBuffer中，并生成两个纹理
 * 一个为数据源，一个为输出源
 * 把数据源的纹理Draw Filter后输出到第二个空白纹理中
 */

public class FBORender implements GLSurfaceView.Renderer {
    private Bitmap mBitmap;
    /**
     * 顶点坐标
     */
    private final FloatBuffer mGLCubeBuffer;

    /**
     * 纹理坐标
     */
    private final FloatBuffer mGLTextureBuffer;
    /**
     * OpenGl的滤镜，继承此Filter实现不同的效果
     */
    private GPUImageFilter mFilter;

    /**
     * 保存纹理数据
     */
    private ByteBuffer mBuffer;
    private int[] fFrame = new int[1];
    private int[] fRender = new int[1];
    private int[] fTexture = new int[2];
    private int mOutputHeight;
    private int mOutputWidth;

    public FBORender(GPUImageFilter gpuImageFilter) {
        this.mFilter = gpuImageFilter;
        mGLCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLCubeBuffer.put(CUBE).position(0);

        mGLTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
    }


    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }


    /**
     * 更换设置的Filter
     *
     * @param filter
     */
    public void setFilter(final GPUImageFilter filter) {
        final GPUImageFilter oldFilter = mFilter;
        mFilter = filter;
        if (oldFilter != null) {
            oldFilter.destroy();
        }
        mFilter.init();
        GLES20.glUseProgram(mFilter.getProgram());
        mFilter.onOutputSizeChanged(mOutputWidth, mOutputHeight);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mFilter.init();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mOutputHeight = height;
        mOutputWidth = width;
        GLES20.glUseProgram(mFilter.getProgram());
        mFilter.onOutputSizeChanged(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (mBitmap != null && !mBitmap.isRecycled()) {
            createEnvi();
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fFrame[0]);
            //使FrameBuffer绑定的第二个空白的纹理上，绘制会绘制到第二个纹理上
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                    GLES20.GL_TEXTURE_2D, fTexture[1], 0);
            GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
                    GLES20.GL_RENDERBUFFER, fRender[0]);
            GLES20.glViewport(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
            //以第一个纹理数据源开始Draw
            //把数据转为Rgba存到Buffer里
            mFilter.onDraw(fTexture[0], mGLCubeBuffer, mGLTextureBuffer);
            GLES20.glReadPixels(0, 0, mBitmap.getWidth(), mBitmap.getHeight(), GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE, mBuffer);
            deleteEnvi();
            mBitmap.recycle();
        }
    }


    /**
     * 创建FrameBuffer，RenderBuffer，并创建2个纹理，第一个已与Bitmap绑定，第二个纹理是空的
     * 第一个纹理为数据库源
     */

    public void createEnvi() {
        GLES20.glGenFramebuffers(1, fFrame, 0);
        GLES20.glGenRenderbuffers(1, fRender, 0);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, fRender[0]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16,
                mBitmap.getWidth(), mBitmap.getHeight());
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
                GLES20.GL_RENDERBUFFER, fRender[0]);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
        GLES20.glGenTextures(2, fTexture, 0);
        for (int i = 0; i < 2; i++) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fTexture[i]);
            if (i == 0) {
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mBitmap, 0);
            } else {
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mBitmap.getWidth(), mBitmap.getHeight(),
                        0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
            }
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        }
        mBuffer = ByteBuffer.allocate(mBitmap.getWidth() * mBitmap.getHeight() * 4);
    }

    /**
     * 清空纹理以及对应的RenderBuffer和FrameBuffer
     */
    private void deleteEnvi() {
        GLES20.glDeleteTextures(2, fTexture, 0);
        GLES20.glDeleteRenderbuffers(1, fRender, 0);
        GLES20.glDeleteFramebuffers(1, fFrame, 0);
    }

    public interface onDataCallBack {
        void onCall(ByteBuffer byteBuffer);
    }
}
