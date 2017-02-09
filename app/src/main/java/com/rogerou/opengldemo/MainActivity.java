package com.rogerou.opengldemo;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity {

    private GLSurfaceView mGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGLSurfaceView = (GLSurfaceView) findViewById(R.id.surfaceView);
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setRenderer(new MyRender());
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }


    private class MyRender implements GLSurfaceView.Renderer {
        private Triangle mTriangle;
//        private final float[] mMVPMatrix = new float[16];
//        private final float[] mProjectionMatrix = new float[16];
//        private final float[] mViewMatrix = new float[16];

        @Override
        public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
//            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        }

        @Override
        public void onSurfaceChanged(GL10 gl10, int i, int i1) {
//            GLES20.glViewport(0, 0, i, i1);
            mTriangle = new Triangle();
        }

        @Override
        public void onDrawFrame(GL10 gl10) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
//            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            mTriangle.draw();
        }
    }


    private class Triangle {
        private FloatBuffer mFloatBuffer;


        static final int CORDS_PER_VERTEX = 3;
        final float[] triangleCords = {
                0.0f, 1.0f, 0.0f, // top
                -1.0f, -1.0f, 0.0f, // bottom left
                1.0f, -1.0f, 0.0f,  // bottom right
        };

//        float color[] = {0.63671875f, 0.76953125f, 0.22265625f, 1.0f};
        private final String vertexShaderCode =
                "attribute vec4 vPosition;" +
                        "void main() {" +
                        "  gl_Position = vPosition;" +
                        "}";

        private final String fragmentShaderCode =
                "precision mediump float;\n"
                        + "void main() {\n"
                        + "  gl_FragColor = vec4(0,0,1,1);\n"
                        + "}";
        int mProgram;
        private int mPositionHandle;
        private int mColorHandle;

        public Triangle() {
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(triangleCords.length * 4)
                    .order(ByteOrder.nativeOrder());

            mFloatBuffer = byteBuffer.asFloatBuffer();
            mFloatBuffer.put(triangleCords);
            mFloatBuffer.position(0);


            mProgram = GLES20.glCreateProgram();
            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
            int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
            GLES20.glAttachShader(mProgram, vertexShader);
            GLES20.glAttachShader(mProgram, fragmentShader);
            GLES20.glLinkProgram(mProgram);

            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        }

        public void draw() {
            GLES20.glUseProgram(mProgram);
            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glVertexAttribPointer(mPositionHandle, CORDS_PER_VERTEX, GLES20.GL_FLOAT, false, CORDS_PER_VERTEX * 4, mFloatBuffer);
//            mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
//            GLES20.glUniform4fv(mColorHandle, 1, color, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, triangleCords.length / CORDS_PER_VERTEX);
            GLES20.glDisableVertexAttribArray(mPositionHandle);
        }
    }

    public static int loadShader(int type, String source) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(type, source);
        GLES20.glCompileShader(shader);
        return shader;
    }
}
 