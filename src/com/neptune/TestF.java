package com.neptune;

import com.neptune.api.Facerig;
import com.neptune.api.Grab;
import com.neptune.config.analyze.CaculateInfo;

import java.io.IOException;
import java.util.List;

/**
 * Created by neptune on 16-10-31.
 */
public class TestF {
    public static void main(String[] args) throws IOException {
        Grab.load("/home/neptune/Grab/libcapdetect.so");
        Facerig.load("/home/neptune/Grab/libcapdetect.so");
        Grab.initCapture(1);

        CaculateInfo info = new CaculateInfo();
        Grab.grabCapture(0, 0, info);
        //Grab.quitCapture();
        System.out.println("width:"+info.width+",height:"+info.height+",pixel length:"+info.pixel.length);
        List<String> list = Facerig.facerig(info);
        System.out.println(list.isEmpty());
        list.forEach(System.out::println);
    }
}
