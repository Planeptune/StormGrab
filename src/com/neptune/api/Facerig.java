package com.neptune.api;

import com.neptune.config.analyze.CaculateInfo;

import java.util.List;

/**
 * Created by neptune on 16-9-13.
 * 人脸提取接口
 */
public class Facerig {

    //加载库，必须先执行该方法才能执行该类其他方法
    public static void load(String libPath) {
        System.load(libPath);
    }

    public static native void initFacerig(String modelPath);

    //本地方法，输入是一张图的CaculateInfo，输出是一个本地文件列表
    public static native List<String> facerig(CaculateInfo info);

    public static native void quitFacerig();

    @Deprecated
    public static void main(String[] args) {

    }
}
