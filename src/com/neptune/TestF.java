package com.neptune;

import com.neptune.api.Facerig;
import com.neptune.api.Grab;
import com.neptune.config.analyze.CaculateInfo;

import javax.imageio.stream.FileImageOutputStream;
import java.io.File;
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
        FileImageOutputStream ios = new FileImageOutputStream(new File("/home/neptune/test.jpg"));
        ios.write(info.pixel);
        //Grab.quitCapture();
        System.out.println("width:"+info.width+",height:"+info.height+",pixel length:"+info.pixel.length);
        Facerig.initFacerig("/home/neptune/Grab/model/seeta_fd_frontal_v1.0.bin");
        List<String> list = Facerig.facerig(info);
        System.out.println(list==null);
        if(list!=null)
            list.forEach(System.out::println);
        Grab.quitCapture();
        Facerig.quitFacerig();
    }
}
