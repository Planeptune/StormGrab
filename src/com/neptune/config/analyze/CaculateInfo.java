package com.neptune.config.analyze;

import java.io.Serializable;

/**
 * Created by neptune on 16-9-18.
 * 传入人脸识别的参数
 */
public class CaculateInfo implements Serializable {
    public String key;//hdfs文件路径
    public byte[] pixel;//图片序列
    public int width;
    public int height;
    public String time_stamp;

    public CaculateInfo() {

    }

    public CaculateInfo(String key, byte[] pixel, int width, int height, String time_stamp) {
        this.key = key;
        this.pixel = pixel;
        this.width = width;
        this.height = height;
        this.time_stamp = time_stamp;
    }
}
