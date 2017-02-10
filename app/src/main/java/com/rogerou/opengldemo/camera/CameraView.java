package com.rogerou.opengldemo.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import com.rogerou.opengldemo.GlsurfaceView.MyGLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Administrator on 2017/2/10.
 */

public class CameraView extends MyGLSurfaceView implements SurfaceTexture.OnFrameAvailableListener {

    private SurfaceTexture mSurfaceTexture;

    private int mTextureId = -1;

    public CameraView(Context context) {
        super(context);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

//    private void openCamera() {
//        if (CameraEngine.getCamera() == null)
//            CameraEngine.openCamera();
//        MyCameraInfo info = CameraEngine.getCameraInfo();
//        if (info.orientation == 90 || info.orientation == 270) {
//            mImageWidth = info.previewHeight;
//            mImageHeight = info.previewWidth;
//        } else {
//            mImageWidth = info.previewWidth;
//            mImageHeight = info.previewHeight;
//        }
//        if (mSurfaceTexture != null)
//            CameraEngine.startPreview(mSurfaceTexture);
//    }


    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        super.onSurfaceCreated(gl10, eglConfig);
//        mTextureId = OpenGlUtils.getExternalOESTextureID();
        if (mTextureId != -1) {
            mSurfaceTexture = new SurfaceTexture(0);
            mSurfaceTexture.setOnFrameAvailableListener(this);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        super.onDrawFrame(gl10);
        if (mSurfaceTexture != null)
            mSurfaceTexture.updateTexImage();
    }


    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        requestRender();
    }
}
