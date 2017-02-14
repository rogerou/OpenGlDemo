package com.rogerou.opengldemo.filter;

import android.opengl.GLES20;

import java.nio.FloatBuffer;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

/**
 * Created by Administrator on 2017/2/14.
 */

public class GroupFilter extends GPUImageFilter {
    protected static int[] sFrameBuffers = null;
    protected static int[] sFrameBufferTextures = null;
    private int frameWidth = -1;
    private int frameHeight = -1;
    protected List<GPUImageFilter> filters;


    public GroupFilter(List<GPUImageFilter> filters) {
        this.filters = filters;
    }


    @Override
    public void onOutputSizeChanged(int width, int height) {
        super.onOutputSizeChanged(width, height);
        int size = filters.size();
        for (int i = 0; i < size; i++) {
            filters.get(i).onOutputSizeChanged(width, height);
        }
        //检查宽高以及Filter数量是否变化，是就重新初始化
        if (sFrameBuffers != null && (frameWidth != width || frameHeight != height || sFrameBuffers.length != size - 1)) {
            destroyFrameBuffers();
            frameWidth = width;
            frameHeight = height;
        }
        //初始化FrameBuffer
        if (sFrameBuffers == null) {
            sFrameBuffers = new int[size - 1];
            sFrameBufferTextures = new int[size - 1];

            for (int i = 0; i < size - 1; i++) {
                //创建FrameBuffer
                GLES20.glGenFramebuffers(1, sFrameBuffers, i);
                /*  
                 生成并绑定到当前纹理，做初始化设置，因为
                 我们必须往里面加入至少一个附件（颜色、深度、模板缓冲）。
                 其中至少有一个是颜色附件。
                 所有的附件都应该是已经完全做好的（已经存储在内存之中）。
                 每个缓冲都应该有同样数目的样本。
                 附件可以是纹理或渲染缓冲（renderbuffer）对象
                 */
                GLES20.glGenTextures(1, sFrameBufferTextures, i);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, sFrameBufferTextures[i]);
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                        GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                        GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                        GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                        GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                        GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);


                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, sFrameBuffers[i]);
                //绑定纹理到FrameBuffer
                GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                        GLES20.GL_TEXTURE_2D, sFrameBufferTextures[i], 0);
                
                
                //解绑纹理和FrameBuffer
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            }
        }

    }

    @Override
    public void onDraw(int textureId, FloatBuffer cubeBuffer, FloatBuffer textureBuffer) {
        if (sFrameBuffers == null || sFrameBufferTextures == null) {
                return;
        }
        super.onDraw(textureId, cubeBuffer, textureBuffer);
    }

    @Override
    public void onInit() {
        for (GPUImageFilter filter : filters) {
            filter.init();
        }
    }

    @Override
    public void onDestroy() {
        for (GPUImageFilter filter : filters) {
            filter.onDestroy();
        }
        destroyFrameBuffers();
    }

    private void destroyFrameBuffers() {
        if (sFrameBufferTextures != null) {
            GLES20.glDeleteTextures(sFrameBufferTextures.length, sFrameBufferTextures, 0);
            sFrameBufferTextures = null;
        }
        if (sFrameBuffers != null) {
            GLES20.glDeleteFramebuffers(sFrameBuffers.length, sFrameBuffers, 0);
            sFrameBuffers = null;
        }

    }


}
