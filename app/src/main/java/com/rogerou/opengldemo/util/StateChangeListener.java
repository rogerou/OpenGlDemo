/*
 *
 * StateCallback.java
 * 
 * Created by Wuwang on 2016/11/30
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.rogerou.opengldemo.util;

/**
 * Description:
 */
public interface StateChangeListener {

    int START = 1;
    int STOP = 2;
    int PLAYING = 3;

    void onStateChanged(int lastState, int nowState);

}
