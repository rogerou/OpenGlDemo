package com.rogerou.opengldemo;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.rogerou.opengldemo.camera.MyCameraManger;
import com.rogerou.opengldemo.controller.OpenGlController;
import com.rogerou.opengldemo.filter.GPUImageFilter;
import com.rogerou.opengldemo.filter.GroupFilter;
import com.rogerou.opengldemo.filter.MyCameraFrameFilter;
import com.rogerou.opengldemo.filter.MyImageFilter;
import com.rogerou.opengldemo.util.StateChangeListener;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2017/2/9.
 */

public class FrameCameraActivity extends AppCompatActivity implements View.OnClickListener {


    private GLSurfaceView glview1;
    private Button btn1;
    private OpenGlController mOpenGlController;
    private MyCameraManger mCameraManger;
    private Camera mCamera;
    private boolean isFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        this.btn1 = (Button) findViewById(R.id.btn1);
        this.glview1 = (GLSurfaceView) findViewById(R.id.gl_view1);
        btn1.setOnClickListener(this);
        mOpenGlController = new OpenGlController(this);
        mOpenGlController.setGLSurfaceView1(glview1);
        mCameraManger = new MyCameraManger();
        setUpCamera();
    }


    //初始化Camera
    public void setUpCamera() {
        mCamera = mCameraManger.getCamera();
        if (mCamera == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        //设置预览输出数据格式
        parameters.setPreviewFormat(ImageFormat.NV21);
        mCamera.setParameters(parameters);
        int orientation = mCameraManger.getCameraDisplayOrientation(this, 0);
        MyCameraManger.MyCameraInfo myCameraInfo = new MyCameraManger.MyCameraInfo();
        mCameraManger.getCameraInfo(0, myCameraInfo);
        boolean flipHorizontal = myCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
        mOpenGlController.setUpCamera(mCamera, orientation, flipHorizontal, false);
    }


    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onClick(View view) {
        //实现多重渲染效果,但是发现只是渲染在
        List<GPUImageFilter> gpuImageFilters = new ArrayList<>();
        switch (view.getId()) {
            case R.id.btn1:
                btn1.setText(isFilter ? "开启滤镜" : "关闭滤镜");
                if (isFilter) {
                    gpuImageFilters.add(new GPUImageFilter());
                } else {
                    MyCameraFrameFilter myFrameAnimationFilter = new MyCameraFrameFilter(getAssets());
                    myFrameAnimationFilter.setStateChangeListener((lastState, nowState) -> {
                        if (nowState == StateChangeListener.STOP) {
                            myFrameAnimationFilter.setAnimation(glview1, "assets/cc.zip", 50);
                            myFrameAnimationFilter.start();
                        }
                    });
                    myFrameAnimationFilter.setAnimation(glview1, "assets/cc.zip", 50);
                    myFrameAnimationFilter.start();
                    gpuImageFilters.add(myFrameAnimationFilter);
                    gpuImageFilters.add(new MyImageFilter());
                }
                mOpenGlController.setFilter(new GroupFilter(gpuImageFilters));
                isFilter = !isFilter;

        }

    }

}

