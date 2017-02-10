package com.rogerou.opengldemo.render;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
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
    private GPUImageFilter mFilter;
    /**
     * 配合GlSurfaceView 预览的所需SurfaceTexture
     */
    private SurfaceTexture mSurfaceTexture = null;
    /**
     * 顶点坐标
     */
    private final FloatBuffer mGLCubeBuffer;

    /**
     * 纹理坐标
     */
    private final FloatBuffer mGLTextureBuffer;
    /**
     * 纹理Id
     */
    private int mGLTextureId = -1;
    /**
     * 两个Draw队列
     * 一个是Draw时
     * 一个是Draw后
     */
    private final Queue<Runnable> runOnDraw;
    private final Queue<Runnable> runOnDrawEnd;
    /**
     * Surface的宽高
     */
    private int mOutputWidth;
    private int mOutputHeight;
    /**
     * 用来创建RGBA的纹理
     */
    private IntBuffer mIntBuffer;
    //是否应该水平旋转
    private boolean mFlipHorizontal;
    //是否应该垂直旋转
    private boolean mFlipVertical;
    //旋转的角度
    private Rotation mRotation;
    //显示内容的宽高
    private int mImageWidth;
    private int mImageHeight;
    private GPUImage.ScaleType mScaleType;

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
                //把数据添加到Camera中
                camera.addCallbackBuffer(bytes);

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

    private void runAll(Queue<Runnable> queue) {
        synchronized (queue) {
            while (!queue.isEmpty()) {
                queue.poll().run();
            }
        }
    }

    //加入Draw队列
    private void addOnDraw(Runnable runnable) {
        synchronized (runOnDraw) {
            runOnDraw.add(runnable);
        }
    }

    //加入DrawEnd队列
    private void addOnDrawEnd(Runnable runnable) {
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
    private void adjustImageScaling() {
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
        if (mScaleType == GPUImage.ScaleType.CENTER_CROP) {
            float distHorizontal = (1 - 1 / ratioWidth) / 2;
            float distVertical = (1 - 1 / ratioHeight) / 2;
            textureCords = new float[]{
                    addDistance(textureCords[0], distHorizontal), addDistance(textureCords[1], distVertical),
                    addDistance(textureCords[2], distHorizontal), addDistance(textureCords[3], distVertical),
                    addDistance(textureCords[4], distHorizontal), addDistance(textureCords[5], distVertical),
                    addDistance(textureCords[6], distHorizontal), addDistance(textureCords[7], distVertical),
            };
        } else {
            cube = new float[]{
                    CUBE[0] / ratioHeight, CUBE[1] / ratioWidth,
                    CUBE[2] / ratioHeight, CUBE[3] / ratioWidth,
                    CUBE[4] / ratioHeight, CUBE[5] / ratioWidth,
                    CUBE[6] / ratioHeight, CUBE[7] / ratioWidth,
            };
        }

        mGLCubeBuffer.clear();
        mGLCubeBuffer.put(cube).position(0);
        mGLTextureBuffer.clear();
        mGLTextureBuffer.put(textureCords).position(0);
    }

    private float addDistance(float coordinate, float distance) {
        return coordinate == 0.0f ? distance : 1 - distance;
    }
}
