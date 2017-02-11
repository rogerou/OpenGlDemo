package com.rogerou.opengldemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Bundle;
import android.support.annotation.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Administrator on 2017/2/9.
 */

public class DemoActivity extends Activity {

    private GLSurfaceView surface;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        surface = new GLSurfaceView(this);
        surface.setEGLContextClientVersion(2);
        surface.setRenderer(new MyRender());
        setContentView(surface);
    }


    private class MyRender implements GLSurfaceView.Renderer {

        private int vertexHandle;
        private int fragmentHandle;
        private Bitmap mBitmap;

        private static final String VERTEX_SHADER = "attribute vec4 vPosition;\n" +
                "attribute vec2 vCoordinate;\n" +
                "uniform mat4 vMatrix;\n" +
                "\n" +
                "varying vec2 aCoordinate;\n" +
                "\n" +
                "void main(){\n" +
                "    gl_Position=vMatrix*vPosition;\n" +
                "    aCoordinate=vCoordinate;\n" +
                "}";
        private static final String FRAGMENT_SHADER = "uniform sampler2D vTexture;\n" +
                "varying vec2 aCoordinate;\n" +
                "\n" +
                "void main(){\n" +
                "    gl_FragColor=texture2D(vTexture,aCoordinate);\n" +
                "}";
        private final float[] sPos = {
                -1.0f, 1.0f,    //左上角
                -1.0f, -1.0f,   //左下角
                1.0f, 1.0f,     //右上角
                1.0f, -1.0f     //右下角
        };

        private final float[] sCoord = {
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 0.0f,
                1.0f, 1.0f,
        };
        private final FloatBuffer mVertexBuffer;
        private final FloatBuffer mUvTexVertexBuffer;
        int mProgram;
        private int mPositionHandle;
        private int mTexCoordHandle;
        private int mTexSamplerHandle;
        private float[] mProjectMatrix = new float[16];
        private float[] mViewMatrix = new float[16];
        private float[] mMVPMatrix = new float[16];
        private int glHMatrix;
        private int glHPosition;
        private int glHCoordinate;
        private int glHTexture;
        private int textureId;

        public MyRender() {
            mVertexBuffer = ByteBuffer.allocateDirect(sPos.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(sPos);
            mVertexBuffer.position(0);

            mUvTexVertexBuffer = ByteBuffer.allocateDirect(sCoord.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(sCoord);
            mUvTexVertexBuffer.position(0);
            mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background_profile);
        }

        private void setup() {
            vertexHandle = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
            fragmentHandle = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
            mProgram = GLES20.glCreateProgram();
            GLES20.glAttachShader(mProgram, vertexHandle);
            GLES20.glAttachShader(mProgram, fragmentHandle);
            GLES20.glLinkProgram(mProgram);
            glHPosition = GLES20.glGetAttribLocation(mProgram, "vPosition");
            glHCoordinate = GLES20.glGetAttribLocation(mProgram, "vCoordinate");
            glHTexture = GLES20.glGetUniformLocation(mProgram, "vTexture");
            glHMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix");


        }

        @Override
        public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
            setup();
        }

        @Override
        public void onSurfaceChanged(GL10 gl10, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
            int w = mBitmap.getWidth();
            int h = mBitmap.getHeight();
            float sWH = w / (float) h;
            float sWidthHeight = width / (float) height;
            if (width > height) {
                if (sWH > sWidthHeight) {
                    Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight * sWH, sWidthHeight * sWH, -1, 1, 3, 7);
                } else {
                    Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight / sWH, sWidthHeight / sWH, -1, 1, 3, 7);
                }
            } else {
                if (sWH > sWidthHeight) {
                    Matrix.orthoM(mProjectMatrix, 0, -1, 1, -1 / sWidthHeight * sWH, 1 / sWidthHeight * sWH, 3, 7);
                } else {
                    Matrix.orthoM(mProjectMatrix, 0, -1, 1, -sWH / sWidthHeight, sWH / sWidthHeight, 3, 7);
                }
            }
            //设置相机位置
            Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
            //计算变换矩阵
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
        }

        @Override
        public void onDrawFrame(GL10 gl10) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            GLES20.glUseProgram(mProgram);
            GLES20.glUniformMatrix4fv(glHMatrix, 1, false, mMVPMatrix, 0);
            GLES20.glEnableVertexAttribArray(glHPosition);
            GLES20.glEnableVertexAttribArray(glHCoordinate);
            GLES20.glUniform1i(glHTexture, 0);
            textureId = createTexture();
            GLES20.glVertexAttribPointer(glHPosition, 2, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
            //传入纹理坐标
            GLES20.glVertexAttribPointer(glHCoordinate,2,GLES20.GL_FLOAT,false,0,mUvTexVertexBuffer);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);
        }

        //加载Shader代码
        int loadShader(int type, String shaderCode) {
            int shader = GLES20.glCreateShader(type);
            GLES20.glShaderSource(shader, shaderCode);
            GLES20.glCompileShader(shader);
            return shader;
        }

        private int createTexture() {
            int[] texture = new int[1];
            if (mBitmap != null && !mBitmap.isRecycled()) {
                //生成纹理
                GLES20.glGenTextures(1, texture, 0);
                //生成纹理
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
                //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
                //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
                //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
                //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
                //根据以上指定的参数，生成一个2D纹理
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);
                return texture[0];
            }
            return 0;
        }
    }
}
