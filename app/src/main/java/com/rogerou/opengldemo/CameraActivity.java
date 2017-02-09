package com.rogerou.opengldemo;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;

/**
 * Created by Administrator on 2017/2/9.
 */

public class CameraActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new CameraView(this));
    }
}
