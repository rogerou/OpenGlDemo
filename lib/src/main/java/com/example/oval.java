package com.example;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/2/10.
 */

public class oval {
    public static void main(String[] args) {
        float[] floats = createPositions();

        for (float aFloat : floats) {
            System.out.println(aFloat);
        }
    }

    private static float[] createPositions() {
        ArrayList<Float> data = new ArrayList<>();
        data.add(0.0f);             //设置圆心坐标
        data.add(0.0f);
        data.add(0.0f);
        float angDegSpan = 360f / 6;
        for (float i = 0; i < 360 + angDegSpan; i += angDegSpan) {
            data.add((float) (1.0f * Math.sin(i * Math.PI / 180f)));
            data.add((float) (1.0f * Math.cos(i * Math.PI / 180f)));
            data.add(0.0f);
        }
        float[] f = new float[data.size()];
        for (int i = 0; i < f.length; i++) {
            f[i] = data.get(i);
        }
        return f;
    }
}
