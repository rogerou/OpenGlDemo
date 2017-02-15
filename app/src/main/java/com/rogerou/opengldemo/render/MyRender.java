package com.rogerou.opengldemo.render;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.rogerou.opengldemo.filter.GPUImageFilter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import jp.co.cyberagent.android.gpuimage.GPUImageNativeLibrary;
import jp.co.cyberagent.android.gpuimage.OpenGlUtils;
import jp.co.cyberagent.android.gpuimage.Rotation;
import jp.co.cyberagent.android.gpuimage.util.TextureRotationUtil;

import static com.rogerou.opengldemo.Constant.CUBE;
import static jp.co.cyberagent.android.gpuimage.util.TextureRotationUtil.TEXTURE_NO_ROTATION;

/**
 * Created by Administrator on 2017/2/10.
 */

public class MyRender implements GLSurfaceView.Renderer, Camera.PreviewCallback {

    /**
     * OpenGl的滤镜，继承此Filter实现不同的效果
     */
    GPUImageFilter mFilter;
    /**
     * 配合GlSurfaceView 预览的所需SurfaceTexture
     */
    SurfaceTexture mSurfaceTexture = null;
    /**
     * 顶点坐标
     */
    FloatBuffer mGLCubeBuffer;

    /**
     * 纹理坐标
     */
    FloatBuffer mGLTextureBuffer;
    /**
     * 纹理Id
     */
    int mGLTextureId = -1;
    /**
     * 两个Draw队列
     * 一个是Draw时
     * 一个是Draw后
     */
    Queue<Runnable> runOnDraw;
    Queue<Runnable> runOnDrawEnd;
    /**
     * Surface的宽高
     */
    int mOutputWidth;
    int mOutputHeight;
    /**
     * 用来创建RGBA的纹理
     */
    IntBuffer mIntBuffer;
    //是否应该水平旋转
    boolean mFlipHorizontal;
    //是否应该垂直旋转
    boolean mFlipVertical;
    //旋转的角度
    Rotation mRotation;
    //显示内容的宽高
    int mImageWidth;
    int mImageHeight;

    public MyRender(GPUImageFilter gpuImageFilter) {
        mFilter = gpuImageFilter;
        mGLCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLCubeBuffer.put(CUBE).position(0);

        mGLTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        runOnDraw = new LinkedList<>();
        runOnDrawEnd = new LinkedList<>();
    }

    /**
     * Camera 回调的每一帧数据都经过这个处理
     * 默认摄像头回调的是N21格式的YUV数据需要转为RGBA格式
     * 可以通过 YUVImage 直接生成对应的图片
     * 在这个回调负责用OpenGl渲染每一帧数据
     * 再到GlSurfaceView呈现
     *
     * @param bytes
     * @param camera
     */
    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        final Camera.Size previewSize = camera.getParameters().getPreviewSize();
        if (mIntBuffer == null) {
            mIntBuffer = IntBuffer.allocate(previewSize.width * previewSize.height);
        }
        if (runOnDraw.isEmpty()) {
            addOnDraw(() -> {
                //通过软解码把Camera每帧的YUV数据转为RGBA的格式
                GPUImageNativeLibrary.YUVtoRBGA(bytes, previewSize.width, previewSize.height, mIntBuffer.array());
                //绑定纹理Id
                mGLTextureId = OpenGlUtils.loadTexture(mIntBuffer, previewSize, mGLTextureId);

                //重新调整宽高
                if (mImageWidth != previewSize.width) {
                    mImageWidth = previewSize.width;
                    mImageHeight = previewSize.height;
                    adjustImageScaling();
                }
            });
        }

    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        //初始化清除颜色
        GLES20.glClearColor(0, 0, 0, 1);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        mFilter.init();
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        mOutputWidth = width;
        mOutputHeight = height;
        GLES20.glViewport(0, 0, width, height);
        GLES20.glUseProgram(mFilter.getProgram());
        mFilter.onOutputSizeChanged(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        runAll(runOnDraw);
        //把效果绘制当前选择的纹理当中
        mFilter.onDraw(mGLTextureId, mGLCubeBuffer, mGLTextureBuffer);
        runAll(runOnDrawEnd);
        if (mSurfaceTexture != null) {
            //刷新Surface显示
            mSurfaceTexture.updateTexImage();
        }
    }

    /**
     * 更换设置的Filter
     *
     * @param filter
     */
    public void setFilter(final GPUImageFilter filter) {
        addOnDraw(() -> {
            final GPUImageFilter oldFilter = mFilter;
            mFilter = filter;
            if (oldFilter != null) {
                oldFilter.destroy();
            }
            mFilter.init();
            GLES20.glUseProgram(mFilter.getProgram());
            mFilter.onOutputSizeChanged(mOutputWidth, mOutputHeight);
        });
    }

    /**
     * 初始化需要实现的SurfaceTexture
     * 并设置到Camera
     *
     * @param camera
     */
    public void setUpSurfaceTexture(final Camera camera) {
        addOnDraw(() -> {
            int[] textures = new int[1];
            GLES20.glGenTextures(1, textures, 0);
            mSurfaceTexture = new SurfaceTexture(textures[0]);
            try {
                camera.setPreviewTexture(mSurfaceTexture);
                camera.setPreviewCallback(MyRender.this);
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    void runAll(Queue<Runnable> queue) {
        synchronized (queue) {
            while (!queue.isEmpty()) {
                queue.poll().run();
            }
        }
    }

    //加入Draw队列
    void addOnDraw(Runnable runnable) {
        synchronized (runOnDraw) {
            runOnDraw.add(runnable);
        }
    }

    //加入DrawEnd队列
    void addOnDrawEnd(Runnable runnable) {
        synchronized (runOnDrawEnd) {
            runOnDrawEnd.add(runnable);
        }
    }

    public void setRotation(final Rotation rotation,
                            final boolean flipHorizontal, final boolean flipVertical) {
        this.mRotation = rotation;
        mFlipHorizontal = flipHorizontal;
        mFlipVertical = flipVertical;

    }

    /**
     * 根据对比屏幕内容和屏幕大小比例放缩纹理
     */
    void adjustImageScaling() {
        float outputWidth = mOutputWidth;
        float outputHeight = mOutputHeight;
        if (mRotation == Rotation.ROTATION_270 || mRotation == Rotation.ROTATION_90) {
            outputWidth = mOutputHeight;
            outputHeight = mOutputWidth;
        }

        float ratio1 = outputWidth / mImageWidth;
        float ratio2 = outputHeight / mImageHeight;
        float ratioMax = Math.max(ratio1, ratio2);
        int imageWidthNew = Math.round(mImageWidth * ratioMax);
        int imageHeightNew = Math.round(mImageHeight * ratioMax);

        float ratioWidth = imageWidthNew / outputWidth;
        float ratioHeight = imageHeightNew / outputHeight;

        float[] cube = CUBE;
        //根据角度 以及是否选择水平或者垂直旋转 纹理坐标 
        float[] textureCords = TextureRotationUtil.getRotation(mRotation, mFlipHorizontal, mFlipVertical);
        cube = new float[]{
                CUBE[0] / ratioHeight, CUBE[1] / ratioWidth,
                CUBE[2] / ratioHeight, CUBE[3] / ratioWidth,
                CUBE[4] / ratioHeight, CUBE[5] / ratioWidth,
                CUBE[6] / ratioHeight, CUBE[7] / ratioWidth,
        };

        mGLCubeBuffer.clear();
        mGLCubeBuffer.put(cube).position(0);
        mGLTextureBuffer.clear();
        mGLTextureBuffer.put(textureCords).position(0);
    }
}
