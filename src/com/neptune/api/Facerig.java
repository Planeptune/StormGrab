package com.neptune.api;

import com.neptune.config.facerig.CaculatePicture;

import java.util.Arrays;
import java.util.List;

/**
 * Created by neptune on 16-9-13.
 * 人脸提取接口（假定）
 */
public class Facerig {

    //TODO 载入库
    /*static{
        System.loadLibrary("");
    }*/

    //返回值是本地文件的路径
    public static List<String> facerig(CaculatePicture info) {
        return Arrays.asList("/home/neptune/src.png");
    }

    //public static native List<String> facerig(CaculatePicture info);
}
