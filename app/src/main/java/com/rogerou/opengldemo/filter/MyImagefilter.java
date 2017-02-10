package com.rogerou.opengldemo.filter;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

/**
 * Created by Administrator on 2017/2/10.
 */

public class MyImageFilter extends GPUImageFilter {

    /**
     * uniform 由外部程序传递给 shader，就像是C语言里面的常量，shader 只能用不能改；
     * attribute 是只能在 vertex shader 中使用的变量；
     * varying 变量是 vertex 和 fragment shader 之间做数据传递用的。
     * 这里做了个简单的滤镜效果是把获取的的RBG三色的反色
     * 即用255-B,255-R,255-R 所得的结果
     */
    private static final String FRAGMENT_SHADER = "precision mediump float;" +
            "varying highp vec2 textureCoordinate;\n" +
            " \n" +
            " uniform sampler2D inputImageTexture;\n" +
            " \n" +
            " void main()\n" +
            " {\n" +
            "     lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "     \n" +
            "     gl_FragColor = vec4((vec3(1.0f) - textureColor.rgb), textureColor.w);\n" +
            " }";

    public MyImageFilter() {
        super(NO_FILTER_VERTEX_SHADER, FRAGMENT_SHADER);
    }
}
