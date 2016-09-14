package com.neptune.api;

import com.neptune.util.ImageBase64;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by neptune on 16-9-13.
 */
public class Facerig implements FacerigImpl {
    @Override
    public List<String> facerig(BufferedImage source) {
        List<String> list = new ArrayList<>();
        list.add(ImageBase64.encoding(source));
        return list;
    }
}
