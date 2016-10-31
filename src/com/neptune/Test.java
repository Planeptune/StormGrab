package com.neptune;

import com.neptune.api.Grab;
import com.neptune.config.analyze.CaculateInfo;

import javax.imageio.stream.FileImageOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Created by neptune on 16-10-20.
 */
public class Test {
    public static void main(String[] args) throws IOException {
        Grab.load("/home/neptune/Grab/libcapdetect.so");
        System.out.println("load");
        Grab.initCapture(1);
        System.out.println("init");
        CaculateInfo info = new CaculateInfo();
        Grab.grabCapture(0, 0, info);
        System.out.println("grab");
        Grab.quitCapture();
        System.out.println("quit");
        FileImageOutputStream ios = new FileImageOutputStream(new File("/home/neptune/test.jpg"));
        ios.write(info.pixel);
    }
}
