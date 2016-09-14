package com.neptune.config.facerig;

import java.io.Serializable;

/**
 * Created by neptune on 16-9-14.
 * 图片处理结果，与人脸提取、人脸识别算法的接口数据类型
 */
public class CaculatePicture implements Serializable {
    public String url;//图片地址
    public byte[] value;//图片序列
    public int width;//图片宽度（列数）
    public int height;//图片高度（行数）

    public CaculatePicture(String url, byte[] value, int width, int height) {
        this.url = url;
        this.value = value;
        this.width = width;
        this.height = height;
    }
}
