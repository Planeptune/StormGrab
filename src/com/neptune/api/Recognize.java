package com.neptune.api;

/**
 * Created by neptune on 16-10-10.
 */
public class Recognize {
    //本地方法，识别是否为黑名单，返回值为黑名单id，-1表示不是黑名单
    public static native int recognize(float[] features);

    public static void main(String[] args) {

    }
}
