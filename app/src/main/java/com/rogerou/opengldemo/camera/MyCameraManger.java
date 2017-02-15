package com.rogerou.opengldemo.camera;

import android.app.Activity;
import android.hardware.Camera;
import android.view.Surface;


/**
 * Created by Administrator on 2017/2/10.
 */

public class MyCameraManger {

    private Camera mCamera;


    public MyCameraManger() {
        mCamera = Camera.open();
    }

    public Camera getCamera() {
        return mCamera;
    }


    public void releaseCamera() {
        mCamera.setPreviewCallback(null);
        mCamera.release();
        mCamera = null;
    }


    public int getCameraDisplayOrientation(final Activity activity, final int cameraId) {
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (cameraInfo.orientation + degrees) % 360;
        } else {
            result = (cameraInfo.orientation - degrees + 360) % 360;
        }
        return result;
    }


    public void getCameraInfo(int cameraId, MyCameraInfo myCameraInfo) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);
        myCameraInfo.facing = cameraInfo.facing;
        myCameraInfo.orientation = cameraInfo.orientation;
    }

    public static class MyCameraInfo {
        public int facing;
        public int orientation;
    }
}

