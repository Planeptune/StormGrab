package com.neptune;

import com.neptune.api.Grab;
import com.neptune.config.analyze.CaculateInfo;

/**
 * Created by neptune on 16-10-20.
 */
public class Test {
    public static void main(String[] args) {
        //Grab.load("/home/neptune/Grab/lib/netsdk/libavnetsdk.so");
        //Grab.load("/home/neptune/Grab/lib/netsdk/libdhdvr.so");
        //Grab.load("/home/neptune/Grab/lib/netsdk/libdhnetsdk.so");
        Grab.load("/home/neptune/Grab/libcapdetect.so");
        System.out.println("load");
        Grab.initCapture(0);
        System.out.println("init");
        CaculateInfo info = new CaculateInfo();
        info.key = "";
        info.time_stamp = "";
        info.pixel = new byte[10000];
        Grab.grabCapture(0, 1, info);
        System.out.println("grab");
        Grab.quitCapture();
        System.out.println("quit");
        System.out.println("print: " + info.height + "," + info.time_stamp);
    }
}
