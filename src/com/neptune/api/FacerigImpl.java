package com.neptune.api;

import com.neptune.config.facerig.CaculatePicture;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.List;

/**
 * Created by neptune on 16-9-13.
 * 人脸提取接口
 */
public interface FacerigImpl extends Serializable {
    public List<String> facerig(CaculatePicture info);
}
