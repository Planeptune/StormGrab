package com.neptune.api;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.List;

/**
 * Created by neptune on 16-9-13.
 * 人脸分离接口
 */
public interface FacerigImpl extends Serializable {
    public List<String> facerig(BufferedImage source);
}
