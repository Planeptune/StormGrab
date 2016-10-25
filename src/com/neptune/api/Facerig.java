package com.neptune.api;

import com.neptune.config.analyze.CaculateInfo;

import java.util.List;

/**
 * Created by neptune on 16-9-13.
 * 人脸提取接口
 */
public class Facerig {

    static {
        //System.loadLibrary("facerig");
    }

    //本地方法，输入是一张图的CaculateInfo，输出是一个本地文件列表
    public static native List<String> facerig(CaculateInfo info);

    @Deprecated
    public static void main(String[] args) {

    }
}
